/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector.addConnectionToConfig;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the JMS Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsOutboundEndpoint extends AbstractJmsEndpoint {

  public static final String XPATH_SELECTOR = "/mule:mule//jms:outbound-endpoint";

  @Override
  public String getDescription() {
    return "Update JMS transport outbound endpoint.";
  }

  public JmsOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JMS_NAMESPACE));
  }

  private String mapTransactionalAction(String action, MigrationReport report, Element tx, Element object) {
    // Values defined in org.mule.runtime.extension.api.tx.OperationTransactionalAction
    if ("NONE".equals(action)) {
      return "NOT_SUPPORTED";
    } else if ("ALWAYS_BEGIN".equals(action)) {
      report.report(ERROR, tx, object, "Use a <try> scope to begin a nested transaction.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/try-scope-xml-reference#properties-of-try");
      return "ALWAYS_JOIN";
    } else if ("BEGIN_OR_JOIN".equals(action)) {
      return "JOIN_IF_POSSIBLE";
    }

    return action;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    jmsTransportLib(getApplicationModel());

    Element tx = object.getChild("transaction", JMS_NAMESPACE);
    while (tx != null) {
      object.setAttribute("transactionalAction", mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object));
      object.removeChild("transaction", JMS_NAMESPACE);
      tx = object.getChild("transaction", JMS_NAMESPACE);
    }
    while (object.getChild("xa-transaction", CORE_NAMESPACE) != null) {
      Element xaTx = object.getChild("xa-transaction", CORE_NAMESPACE);
      object.setAttribute("transactionalAction", mapTransactionalAction(xaTx.getAttributeValue("action"), report, xaTx, object));

      object.removeChild("xa-transaction", CORE_NAMESPACE);
    }

    final Namespace jmsConnectorNamespace = Namespace.getNamespace("jms", "http://www.mulesoft.org/schema/mule/jms");
    getApplicationModel().addNameSpace(jmsConnectorNamespace, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                       object.getDocument());
    object.setNamespace(jmsConnectorNamespace);

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("one-way")) {
      object.setName("publish");
    } else {

      Element wrappingTry = new Element("try", CORE_NAMESPACE);

      object.getParentElement().addContent(object.getParentElement().indexOf(object), wrappingTry);

      object.detach();
      wrappingTry.addContent(object);

      wrappingTry.addContent(new Element("error-handler", CORE_NAMESPACE)
          .addContent(new Element("on-error-continue", CORE_NAMESPACE)
              .setAttribute("type", "JMS:TIMEOUT")
              .addContent(new Element("set-payload", CORE_NAMESPACE)
                  .setAttribute("value", "#[null]"))));

      object.setName("publish-consume");
    }

    Optional<Element> connector;
    if (object.getAttribute("connector-ref") != null) {
      connector = of(getConnector(object.getAttributeValue("connector-ref")));
      object.removeAttribute("connector-ref");
    } else {
      connector = getDefaultConnector();
    }

    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "JmsConfig");


    Optional<Element> config = getApplicationModel().getNodeOptional("mule:mule/jms:config[@name='" + configName + "']");
    Element jmsConfig = config.orElseGet(() -> {
      Element jmsCfg = new Element("config", jmsConnectorNamespace);
      jmsCfg.setAttribute("name", configName);

      connector.ifPresent(conn -> {
        addConnectionToConfig(jmsCfg, conn, getApplicationModel(), report);
      });

      addTopLevelElement(jmsCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return jmsCfg;
    });

    String destination = processAddress(object, report).map(address -> {
      String path = address.getPath();
      if ("topic".equals(path)) {
        configureTopicPublisher(object);
        return address.getPort();
      } else {
        return path;
      }
    }).orElseGet(() -> {
      if (object.getAttributeValue("queue") != null) {
        return object.getAttributeValue("queue");
      } else {
        configureTopicPublisher(object);
        return object.getAttributeValue("topic");
      }
    });

    report.report(WARN, object, object, "Avoid using properties to set the JMS properties and headers",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#SendingMessages");

    Element outboundBuilder = new Element("message", jmsConnectorNamespace);

    outboundBuilder.addContent(new Element("reply-to", jmsConnectorNamespace)
        .setAttribute("destination", "#[migration::JmsTransport::jmsPublishReplyTo(vars)]"));
    outboundBuilder.addContent(compatibilityProperties(getApplicationModel()));

    outboundBuilder.setAttribute("correlationId", "#[migration::JmsTransport::jmsCorrelationId(correlationId, vars)]");
    object.setAttribute("sendCorrelationId", "#[migration::JmsTransport::jmsSendCorrelationId(vars)]");

    object.addContent(outboundBuilder);

    connector.ifPresent(m3c -> {
      if (m3c.getAttributeValue("persistentDelivery") != null) {
        object.setAttribute("persistentDelivery", m3c.getAttributeValue("persistentDelivery"));
      }

      // This logic comes from JmsMessageDispatcher#dispatchMessage in Mule 3
      if ("true".equals(m3c.getAttributeValue("honorQosHeaders"))) {
        report.report(WARN, m3c, object,
                      "Store the attributes of the source in a variable instead of using the inbound properties",
                      "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#inbound-properties-are-now-attributes",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#sending-messages");
        String defaultDeliveryMode = "true".equals(m3c.getAttributeValue("persistentDelivery")) ? "2" : "1";

        object.setAttribute("persistentDelivery",
                            "#[(vars.compatibility_inboundProperties.JMSDeliveryMode default " + defaultDeliveryMode + ") == 2]");
        object.setAttribute("priority", "#[vars.compatibility_inboundProperties.JMSPriority default 4]");
      }
    });

    if (object.getAttribute("responseTimeout") != null) {
      object.addContent(new Element("consume-configuration", jmsConnectorNamespace)
          .setAttribute("maximumWait", object.getAttributeValue("responseTimeout")));
    }
    object.removeAttribute("responseTimeout");

    object.setAttribute("config-ref", configName);
    if (destination != null) {
      object.setAttribute("destination", destination);
    }
    object.removeAttribute("queue");
    object.removeAttribute("topic");
    object.removeAttribute("name");

    object.removeAttribute("exchange-pattern");
    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true);

    addAttributesToInboundProperties(object, getApplicationModel(), report);
  }

  private void configureTopicPublisher(Element object) {
    object.setAttribute("destinationType", "TOPIC");
  }

}
