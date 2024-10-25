/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleConnectorChildElements;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnect;

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
public class JmsInboundEndpoint extends AbstractJmsEndpoint {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + JMS_NAMESPACE_URI + "' and local-name()='inbound-endpoint']";

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
      report.report("jms.listenerTx", tx, object);
      return "ALWAYS_BEGIN";
    } else if ("ALWAYS_JOIN".equals(action)) {
      report.report("jms.listenerTx", tx, object);
      return "NONE";
    } else if ("JOIN_IF_POSSIBLE".equals(action)) {
      report.report("jms.listenerTx", tx, object);
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
    while (object.getChild("multi-transaction", CORE_EE_NAMESPACE) != null) {
      Element multiTx = object.getChild("multi-transaction", CORE_EE_NAMESPACE);
      String txAction = mapTransactionalAction(multiTx.getAttributeValue("action"), report, multiTx, object);
      object.setAttribute("transactionalAction", txAction);
      object.removeChild("multi-transaction", CORE_EE_NAMESPACE);
    }

    getApplicationModel().addNameSpace(JMS_NAMESPACE, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                       object.getDocument());

    object.setNamespace(JMS_NAMESPACE);
    object.setName("listener");

    Optional<Element> connector = resolveJmsConnector(object, getApplicationModel());
    String configName = migrateJmsConfig(object, report, connector, getApplicationModel());

    connector.ifPresent(m3c -> {
      migrateReconnect(m3c, object, report, object);

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

      handleConnectorChildElements(m3c,
                                   getApplicationModel().getNode("*/*[namespace-uri()='" + JMS_NAMESPACE_URI
                                       + "' and local-name()='config' and @name='" + configName + "']"),
                                   new Element("connection", CORE_NAMESPACE), report);
    });

    String destination = processAddress(object, report).map(address -> {
      String path = address.getPath();
      if ("topic".equals(path)) {
        configureTopicListener(object, JMS_NAMESPACE, connector);
        return address.getPort();
      } else {
        return path;
      }
    }).orElseGet(() -> {
      if (object.getAttributeValue("queue") != null) {
        return object.getAttributeValue("queue");
      } else {
        configureTopicListener(object, JMS_NAMESPACE, connector);
        return object.getAttributeValue("topic");
      }
    });

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("request-response")) {
      Element outboundBuilder = new Element("response", JMS_NAMESPACE);

      outboundBuilder.addContent(mule3Properties(getApplicationModel()));

      outboundBuilder.setAttribute("correlationId", "#[migration::JmsTransport::jmsCorrelationId(correlationId, vars)]");

      report.report("jms.propertiesListener", object, object);

      connector.ifPresent(m3c -> {
        if (m3c.getAttributeValue("persistentDelivery") != null) {
          outboundBuilder.setAttribute("persistentDelivery", m3c.getAttributeValue("persistentDelivery"));
        }
      });

      object.addContent(outboundBuilder);
    }

    if (object.getChild("selector", JMS_NAMESPACE) != null) {
      object.setAttribute("selector", object.getChild("selector", JMS_NAMESPACE).getAttributeValue("expression"));
      object.removeChild("selector", JMS_NAMESPACE);
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
