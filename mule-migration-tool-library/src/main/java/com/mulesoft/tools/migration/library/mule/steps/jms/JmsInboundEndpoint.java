/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
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

  public static final String XPATH_SELECTOR = "/*/mule:flow/jms:inbound-endpoint";

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
      object.removeChild("transaction", JMS_NAMESPACE);
      tx = object.getChild("transaction", JMS_NAMESPACE);
    }
    while (object.getChild("xa-transaction", CORE_NAMESPACE) != null) {
      Element xaTx = object.getChild("xa-transaction", CORE_NAMESPACE);
      String txAction = mapTransactionalAction(xaTx.getAttributeValue("action"), report, xaTx, object);
      object.setAttribute("transactionalAction", txAction);
      object.setAttribute("transactionType", "XA");
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

    Optional<Element> config = getApplicationModel().getNodeOptional("*/jms:config[@name='" + configName + "']");
    Element jmsConfig = config.orElseGet(() -> {
      final Element jmsCfg = new Element("config", jmsConnectorNamespace);
      jmsCfg.setAttribute("name", configName);

      connector.ifPresent(conn -> {
        addConnectionToConfig(jmsCfg, conn, getApplicationModel(), report);
      });

      addTopLevelElement(jmsCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return jmsCfg;
    });

    connector.ifPresent(m3c -> {
      Element reconnectforever = m3c.getChild("reconnect-forever", CORE_NAMESPACE);
      if (reconnectforever != null) {
        object.addContent(new Element("reconnect-forever", CORE_NAMESPACE)
            .setAttribute("frequency", reconnectforever.getAttributeValue("frequency")));
      }

      Element reconnect = m3c.getChild("reconnect", CORE_NAMESPACE);
      if (reconnect != null) {
        object.addContent(new Element("reconnect", CORE_NAMESPACE)
            .setAttribute("frequency", reconnect.getAttributeValue("frequency"))
            .setAttribute("count", reconnect.getAttributeValue("count")));
      }

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

    String destination = processAddress(object, report).map(address -> {
      String path = address.getPath();
      if ("topic".equals(path)) {
        configureTopicListener(object, jmsConnectorNamespace, connector);
        return address.getPort();
      } else {
        return path;
      }
    }).orElseGet(() -> {
      if (object.getAttributeValue("queue") != null) {
        return object.getAttributeValue("queue");
      } else {
        configureTopicListener(object, jmsConnectorNamespace, connector);
        return object.getAttributeValue("topic");
      }
    });

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("request-response")) {
      Element outboundBuilder = new Element("response", jmsConnectorNamespace);

      outboundBuilder.addContent(compatibilityProperties(getApplicationModel()));

      outboundBuilder.setAttribute("correlationId", "#[migration::JmsTransport::jmsCorrelationId(correlationId, vars)]");
      outboundBuilder.setAttribute("sendCorrelationId", "#[migration::JmsTransport::jmsSendCorrelationId(vars)]");

      report.report(WARN, object, object, "Avoid using properties to set the JMS response properties and headers",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#ListeningForNewMessages",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#RespondingToIncommingMessages");

      connector.ifPresent(m3c -> {
        if (m3c.getAttributeValue("persistentDelivery") != null) {
          outboundBuilder.setAttribute("persistentDelivery", m3c.getAttributeValue("persistentDelivery"));
        }
      });

      object.addContent(outboundBuilder);
    }

    if (object.getChild("selector", jmsConnectorNamespace) != null) {
      object.setAttribute("selector", object.getChild("selector", jmsConnectorNamespace).getAttributeValue("expression"));
      object.removeChild("selector", jmsConnectorNamespace);
    }

    object.setAttribute("config-ref", configName);
    if (destination != null) {
      object.setAttribute("destination", destination);
    }
    object.removeAttribute("queue");
    object.removeAttribute("topic");
    object.removeAttribute("name");

    object.removeAttribute("responseTimeout");
    // TODO
    object.removeAttribute("xaPollingTimeout");

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("request-response")) {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, true);
    } else {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, false);
    }

    addAttributesToInboundProperties(object, getApplicationModel(), report);
  }

  private void configureTopicListener(Element object, final Namespace jmsConnectorNamespace, Optional<Element> connector) {
    Element topicConsumer = new Element("topic-consumer", jmsConnectorNamespace);

    connector.ifPresent(m3c -> {
      copyAttributeIfPresent(m3c, topicConsumer, "durable");
      m3c.removeAttribute("durable");
      m3c.removeAttribute("subscriptionName");
    });
    object.setAttribute("numberOfConsumers", "1");

    object.addContent(new Element("consumer-type", jmsConnectorNamespace)
        .addContent(topicConsumer));
  }
}
