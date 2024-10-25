/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleConnectorChildElements;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.hasAttribute;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the AMQP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AmqpInboundEndpoint extends AbstractAmqpEndpoint {

  public static final String XPATH_SELECTOR = "//*[(namespace-uri()='" + AMQP_NAMESPACE_URI
      + "' or namespace-uri()='" + AMQPS_NAMESPACE_URI + "') and local-name()='inbound-endpoint']";

  @Override
  public String getDescription() {
    return "Update AMQP transport inbound endpoint.";
  }

  public AmqpInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(AMQP_NAMESPACE, AMQPS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    object.setNamespace(AMQP_NAMESPACE);
    object.setName("listener");

    Element tx = object.getChild("transaction", AMQP_NAMESPACE);
    while (tx != null) {
      String txAction = mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object);
      object.setAttribute("transactionalAction", txAction);
      copyAttributeIfPresent(tx, object, "recoverStrategy");
      object.removeChild("transaction", AMQP_NAMESPACE);
      tx = object.getChild("transaction", AMQP_NAMESPACE);
    }

    Optional<Element> connector = resolveAmqpConnector(object, getApplicationModel());
    String configName = migrateAmqpConfig(object, report, connector, getApplicationModel());

    connector.ifPresent(m3c -> {
      Element reconnectforever = m3c.getChild("reconnect-forever", CORE_NAMESPACE);
      if (reconnectforever != null) {
        object.addContent(new Element("reconnect-forever", CORE_NAMESPACE).setAttribute("frequency",
                                                                                        reconnectforever
                                                                                            .getAttributeValue("frequency")));
      }

      Element reconnect = m3c.getChild("reconnect", CORE_NAMESPACE);
      if (reconnect != null) {
        object.addContent(new Element("reconnect", CORE_NAMESPACE)
            .setAttribute("frequency", reconnect.getAttributeValue("frequency"))
            .setAttribute("count", reconnect.getAttributeValue("count")));
      }

      handleConnectorChildElements(m3c,
                                   getApplicationModel().getNode("*/*[namespace-uri()='" + AMQP_NAMESPACE_URI
                                       + "' and local-name()='config' and @name='" + configName + "']"),
                                   new Element("connection", CORE_NAMESPACE), report);
    });

    object.setAttribute("config-ref", configName);

    if (hasAttribute(object, "exchangeAutoDelete") || hasAttribute(object, "exchangeDurable") ||
        hasAttribute(object, "exchangeType") || hasAttribute(object, "routingKey")) {
      object.removeAttribute("exchangeAutoDelete");
      object.removeAttribute("exchangeDurable");
      object.removeAttribute("exchangeType");
      object.removeAttribute("routingKey");
      report.report("amqp.exchangeDefinition", object, object);
    }

    copyAttributeIfPresent(object, object, "numberOfChannels", "numberOfConsumers");
    object.removeAttribute("numberOfChannels");

    resolveFallbackQueue(object, report);
  }

  private void resolveFallbackQueue(Element object, MigrationReport report) {
    Element queueDefinition = new Element("fallback-queue-definition", AMQP_NAMESPACE);
    Boolean autoDelete = Boolean.parseBoolean(object.getAttributeValue("queueAutoDelete"));
    Boolean queueDurable = Boolean.parseBoolean(object.getAttributeValue("queueDurable"));
    object.removeAttribute("queueAutoDelete");
    object.removeAttribute("queueDurable");
    String removalStrategy = resolveRemovalStrategy(autoDelete, queueDurable);

    if (removalStrategy != null) {
      queueDefinition.setAttribute("removalStrategy", removalStrategy);
    } else {
      report.report("amqp.queueRemovalStrategy", object, queueDefinition);
    }

    if (hasAttribute(object, "exchangeName")) {
      queueDefinition.setAttribute("exchangeToBind", object.getAttributeValue("exchangeName"));
      object.removeAttribute("exchangeName");
    }

    object.addContent(queueDefinition);

  }

  private String mapTransactionalAction(String action, MigrationReport report, Element tx, Element object) {
    // Values defined in org.mule.runtime.core.api.transaction.TransactionConfig
    if ("NONE".equals(action)) {
      return "NONE";
    } else if ("ALWAYS_BEGIN".equals(action)) {
      return "ALWAYS_BEGIN";
    } else if ("BEGIN_OR_JOIN".equals(action)) {
      report.report("amqp.listenerTx", tx, object);
      return "ALWAYS_BEGIN";
    } else if ("ALWAYS_JOIN".equals(action)) {
      report.report("amqp.listenerTx", tx, object);
      return "NONE";
    } else if ("JOIN_IF_POSSIBLE".equals(action)) {
      report.report("amqp.listenerTx", tx, object);
      return "NONE";
    } else if ("NOT_SUPPORTED".equals(action)) {
      return "NONE";
    }

    return action;
  }

}
