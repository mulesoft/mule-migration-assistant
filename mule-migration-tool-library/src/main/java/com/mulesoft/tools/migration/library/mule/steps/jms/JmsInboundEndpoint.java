/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector.addConnectionToConfig;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the JMS Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsInboundEndpoint extends AbstractJmsEndpoint {

  public static final String XPATH_SELECTOR = "/mule:mule/mule:flow/jms:inbound-endpoint";

  @Override
  public String getDescription() {
    return "Update JMS transport inbound endpoint.";
  }

  public JmsInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JMS_NAMESPACE));
  }

  private String mapTransactionalAction(String action, MigrationReport report, Element tx, Element object) {
    // Values defined in org.mule.runtime.core.api.transaction.TransactionConfig
    if ("NONE".equals(action)) {
      return "NONE";
    } else if ("ALWAYS_BEGIN".equals(action)) {
      return "ALWAYS_BEGIN";
    } else if ("BEGIN_OR_JOIN".equals(action)) {
      report.report(WARN, tx, object,
                    "There can be no transaction active before the listener, so JOIN is not supported at this point.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#UsingTransactions");
      return "ALWAYS_BEGIN";
    } else if ("ALWAYS_JOIN".equals(action)) {
      report.report(WARN, tx, object,
                    "There can be no transaction active before the listener, so JOIN is not supported at this point.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#UsingTransactions");
      return "NONE";
    } else if ("JOIN_IF_POSSIBLE".equals(action)) {
      report.report(WARN, tx, object,
                    "There can be no transaction active before the listener, so JOIN is not supported at this point.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#UsingTransactions");
      return "NONE";
    } else if ("NOT_SUPPORTED".equals(action)) {
      return "NONE";
    }

    return action;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    jmsTransportLib(getApplicationModel());

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    Element tx = object.getChild("transaction", JMS_NAMESPACE);
    while (tx != null) {
      String txAction = mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object);
      object.setAttribute("transactionalAction", txAction);
      // if (!"NONE".equals(txAction)) {
      // if (object.getChild("redelivery-policy", CORE_NAMESPACE) == null) {
      // object.addContent(new Element("redelivery-policy", CORE_NAMESPACE));
      // }
      // }
      object.removeChild("transaction", JMS_NAMESPACE);
      tx = object.getChild("transaction", JMS_NAMESPACE);
    }
    while (object.getChild("xa-transaction", CORE_NAMESPACE) != null) {
      Element xaTx = object.getChild("xa-transaction", CORE_NAMESPACE);
      String txAction = mapTransactionalAction(xaTx.getAttributeValue("action"), report, xaTx, object);
      object.setAttribute("transactionalAction", txAction);
      object.setAttribute("transactionType", "XA");
      // if (!"NONE".equals(txAction)) {
      // if (object.getChild("redelivery-policy", CORE_NAMESPACE) == null) {
      // object.addContent(new Element("redelivery-policy", CORE_NAMESPACE));
      // }
      // }
      //
      // if ("true".equals(xaTx.getAttributeValue("interactWithExternal"))) {
      // report.report(ERROR, xaTx, object, "Mule 4 does not support joining with external transactions.");
      // }
      //
      object.removeChild("xa-transaction", CORE_NAMESPACE);
    }

    final Namespace jmsConnectorNamespace = Namespace.getNamespace("jms", "http://www.mulesoft.org/schema/mule/jms");
    getApplicationModel().addNameSpace(jmsConnectorNamespace, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                       object.getDocument());

    object.setNamespace(jmsConnectorNamespace);
    object.setName("listener");

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
      final Element jmsCfg = new Element("config", jmsConnectorNamespace);
      jmsCfg.setAttribute("name", configName);
      // Element queues = new Element("queues", jmsConnectorNamespace);
      // jmsCfg.addContent(queues);

      connector.ifPresent(conn -> {
        addConnectionToConfig(jmsCfg, conn);
      });

      addTopLevelElement(jmsCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return jmsCfg;
    });

    // String path = processAddress(object, report).map(address -> address.getPath()).orElseGet(() -> obtainPath(object));
    String destination =
        processAddress(object, report).map(address -> address.getPath()).orElseGet(() -> {
          if (object.getAttributeValue("queue") != null) {
            return object.getAttributeValue("queue");
          } else {
            return object.getAttributeValue("topic");
          }
        });

    // addQueue(vmConnectorNamespace, connector, vmConfig, path);
    //
    // connector.ifPresent(conn -> {
    // Integer consumers = null;
    // if (conn.getAttribute("numberOfConcurrentTransactedReceivers") != null) {
    // consumers = parseInt(conn.getAttributeValue("numberOfConcurrentTransactedReceivers"));
    // } else if (conn.getChild("receiver-threading-profile", CORE_NAMESPACE) != null
    // && conn.getChild("receiver-threading-profile", CORE_NAMESPACE).getAttribute("maxThreadsActive") != null) {
    // consumers = parseInt(conn.getChild("receiver-threading-profile", CORE_NAMESPACE).getAttributeValue("maxThreadsActive"));
    // }
    //
    // if (consumers != null) {
    // getFlow(object).setAttribute("maxConcurrency", "" + consumers);
    // object.setAttribute("numberOfConsumers", "" + consumers);
    // }
    // });
    //
    // if (object.getAttribute("mimeType") != null) {
    // Element setMimeType =
    // new Element("set-payload", CORE_NAMESPACE)
    // .setAttribute("value", "#[output " + object.getAttributeValue("mimeType") + " --- payload]");
    //
    // addElementAfter(setMimeType, object);
    // object.removeAttribute("mimeType");
    // }
    //
    // if (object.getAttribute("responseTimeout") != null) {
    // object.setAttribute("timeout", object.getAttributeValue("responseTimeout"));
    // object.setAttribute("timeoutUnit", "MILLISECONDS");
    // object.removeAttribute("responseTimeout");
    // }

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("request-response")) {
      Element outboundBuilder = new Element("response", jmsConnectorNamespace);

      outboundBuilder.addContent(compatibilityProperties(getApplicationModel()));

      outboundBuilder.setAttribute("correlationId", "#[migration::JmsTransport::jmsCorrelationId(correlationId, vars)]");
      // TODO MMT-196 uncomment this
      // response.setAttribute("sendCorrelationId", "#[migration::JmsTransport::jmsSendCorrelationId(vars)]");

      report.report(WARN, object, object, "Avoid using properties to set the JMS response properties and headers",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#ListeningForNewMessages",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#RespondingToIncommingMessages");

      object.addContent(outboundBuilder);
    }

    // connector.ifPresent(m3c -> {
    // // This logic comes from JmsMessageDispatcher#dispatchMessage in Mule 3
    // if ("true".equals(m3c.getAttributeValue("honorQosHeaders"))) {
    // report.report(WARN, m3c, object,
    // "Store the attributes of the source in a variable instead of using the inbound properties",
    // "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#inbound-properties-are-now-attributes",
    // "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#sending-messages");
    // String defaultDeliveryMode = "true".equals(m3c.getAttributeValue("persistentDelivery")) ? "2" : "1";
    //
    // object.setAttribute("persistentDelivery",
    // "#[(vars.compatibility_inboundProperties.JMSDeliveryMode default " + defaultDeliveryMode + ") == 2]");
    // object.setAttribute("priority", "#[vars.compatibility_inboundProperties.JMSPriority default 4]");
    // }
    // });

    object.setAttribute("config-ref", configName);
    if (destination != null) {
      object.setAttribute("destination", destination);
    }
    object.removeAttribute("queue");
    object.removeAttribute("topic");
    object.removeAttribute("name");

    // TODO
    object.removeAttribute("xaPollingTimeout");

    connector.ifPresent(m3c -> {
      if (m3c.getAttributeValue("acknowledgementMode") != null) {
        switch (m3c.getAttributeValue("acknowledgementMode")) {
          case "CLIENT_ACKNOWLEDGE":
            object.setAttribute("ackMode", "MANUAL");
            break;
          case "DUPS_OK_ACKNOWLEDGE":
            object.setAttribute("ackMode", "DUPS_OK");
            break;
          default:
            // AUTO is default, no need to set it
        }
      }

      if (m3c.getAttributeValue("numberOfConsumers") != null) {
        object.setAttribute("numberOfConsumers", m3c.getAttributeValue("numberOfConsumers"));
      }
    });

    // object.removeAttribute("disableTransportTransformer");

    // Element content = buildContent(jmsConnectorNamespace);
    // object.addContent(new Element("response", jmsConnectorNamespace).addContent(content));
    // report.report(WARN, content, content,
    // "You may remove this if this flow is not using sessionVariables, or after those are migrated to variables.",
    // "https://docs.mulesoft.com/mule4-user-guide/v/4.1/intro-mule-message#session-properties");

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("request-response")) {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, true);
    } else {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, false);
    }

    addAttributesToInboundProperties(object, getApplicationModel(), report);
  }
}
