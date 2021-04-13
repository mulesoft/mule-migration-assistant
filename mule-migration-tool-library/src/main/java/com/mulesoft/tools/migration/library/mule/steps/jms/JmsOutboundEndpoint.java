/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleConnectorChildElements;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the inbound endpoint of the JMS Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsOutboundEndpoint extends AbstractJmsEndpoint {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + JMS_NAMESPACE_URI + "' and local-name()='outbound-endpoint']";

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
      report.report("jms.nestedTx", tx, object);
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
    while (object.getChild("multi-transaction", CORE_EE_NAMESPACE) != null) {
      Element multiTx = object.getChild("multi-transaction", CORE_EE_NAMESPACE);
      object.setAttribute("transactionalAction",
                          mapTransactionalAction(multiTx.getAttributeValue("action"), report, multiTx, object));

      object.removeChild("multi-transaction", CORE_EE_NAMESPACE);
    }

    getApplicationModel().addNameSpace(JMS_NAMESPACE, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                       object.getDocument());
    object.setNamespace(JMS_NAMESPACE);

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

    Optional<Element> connector = resolveJmsConnector(object, getApplicationModel());
    String configName = migrateJmsConfig(object, report, connector, getApplicationModel());

    migrateOutboundJmsEndpoint(object, report, connector, configName, getApplicationModel());
    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true);

    addAttributesToInboundProperties(object, getApplicationModel(), report);
  }

  public static void migrateOutboundJmsEndpoint(Element object, MigrationReport report, Optional<Element> connector,
                                                String configName, ApplicationModel appModel) {
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

    report.report("jms.propertiesPublish", object, object);

    Element outboundBuilder = new Element("message", JMS_NAMESPACE);

    Attribute migrationReplyTo = object.getAttribute("reply-to", Namespace.getNamespace("migration", "migration"));
    if (migrationReplyTo != null) {
      if (migrationReplyTo.getValue().startsWith("TOPIC:")) {
        outboundBuilder.addContent(new Element("reply-to", JMS_NAMESPACE)
            .setAttribute("destination", migrationReplyTo.getValue())
            .setAttribute("destinationType", "TOPIC"));
      } else {
        outboundBuilder.addContent(new Element("reply-to", JMS_NAMESPACE)
            .setAttribute("destination", migrationReplyTo.getValue()));
      }

      migrationReplyTo.detach();
    } else {
      outboundBuilder.addContent(new Element("reply-to", JMS_NAMESPACE)
          .setAttribute("destination", "#[migration::JmsTransport::jmsPublishReplyTo(vars)]"));
    }

    outboundBuilder.addContent(compatibilityProperties(appModel));

    outboundBuilder.setAttribute("correlationId", "#[migration::JmsTransport::jmsCorrelationId(correlationId, vars)]");
    object.setAttribute("sendCorrelationId", "#[migration::JmsTransport::jmsSendCorrelationId(vars)]");

    object.addContent(outboundBuilder);

    connector.ifPresent(m3c -> {
      if (m3c.getAttributeValue("persistentDelivery") != null) {
        object.setAttribute("persistentDelivery", m3c.getAttributeValue("persistentDelivery"));
      }

      // This logic comes from JmsMessageDispatcher#dispatchMessage in Mule 3
      if ("true".equals(m3c.getAttributeValue("honorQosHeaders"))) {
        report.report("jms.inboundProperties", m3c, object);
        String defaultDeliveryMode = "true".equals(m3c.getAttributeValue("persistentDelivery")) ? "2" : "1";

        object.setAttribute("persistentDelivery",
                            "#[(vars.compatibility_inboundProperties.JMSDeliveryMode default " + defaultDeliveryMode + ") == 2]");
        object.setAttribute("priority", "#[vars.compatibility_inboundProperties.JMSPriority default 4]");
      }

      handleConnectorChildElements(m3c,
                                   appModel.getNode("*/*[namespace-uri()='" + JMS_NAMESPACE_URI
                                       + "' and local-name()='config' and @name='" + configName + "']"),
                                   new Element("connection", CORE_NAMESPACE), report);
    });

    if (object.getAttribute("responseTimeout") != null) {
      object.addContent(new Element("consume-configuration", JMS_NAMESPACE)
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
  }

  private static void configureTopicPublisher(Element object) {
    object.setAttribute("destinationType", "TOPIC");
  }

}
