/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleConnectorChildElements;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.hasAttribute;
import static java.lang.Boolean.parseBoolean;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Migration for Amqp Outbound.
 * 
 * @author Mulesoft Inc.
 *
 */
public class AmqpOutboundEndpoint extends AbstractAmqpEndpoint {

  public static final String XPATH_SELECTOR = "//*[(namespace-uri()='" + AMQP_NAMESPACE_URI
      + "' or namespace-uri()='" + AMQPS_NAMESPACE_URI + "') and local-name()='outbound-endpoint']";


  @Override
  public String getDescription() {
    return "Update AMQP transport outbound endpoint.";
  }

  public AmqpOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(AMQP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Element tx = object.getChild("transaction", AMQP_NAMESPACE);
    while (tx != null) {
      object.setAttribute("transactionalAction", mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object));
      object.removeChild("transaction", AMQP_NAMESPACE);
      tx = object.getChild("transaction", AMQP_NAMESPACE);
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

    object.setNamespace(AMQP_NAMESPACE);

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("one-way")) {
      object.setName("publish");
    } else {
      object.setName("publish-consume");
    }

    object.removeAttribute("exchange-pattern");

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

    if (hasAttribute(object, "exchangeName") || hasAttribute(object, "exchangeAutoDelete")
        || hasAttribute(object, "exchangeDurable")) {
      resolveFallbackExchange(object, report);
    }
  }

  private void resolveFallbackExchange(Element object, MigrationReport report) {
    Element queueDefinition = new Element("fallback-exchange-definition", AMQP_NAMESPACE);
    Boolean autoDelete = parseBoolean(object.getAttributeValue("exchangeAutoDelete"));
    Boolean queueDurable = parseBoolean(object.getAttributeValue("exchangeDurable"));
    object.removeAttribute("exchangeAutoDelete");
    object.removeAttribute("exchangeDurable");
    String removalStrategy = resolveRemovalStrategy(autoDelete, queueDurable);

    if (removalStrategy != null) {
      queueDefinition.setAttribute("removalStrategy", removalStrategy);
    } else {
      report.report("amqp.exchangeRemovalStrategy", object, queueDefinition);
    }

    if (hasAttribute(object, "exchangeType")) {
      queueDefinition.setAttribute("type", object.getAttributeValue("exchangeType"));
      object.removeAttribute("exchangeType");
    }
    object.addContent(queueDefinition);


    if (hasAttribute(object, "queueAutoDelete") || hasAttribute(object, "queueDurable")
        || hasAttribute(object, "queueExclusive")) {
      report.report("queueDefinitionInPublish", object, queueDefinition);
      object.removeAttribute("queueAutoDelete");
      object.removeAttribute("queueDurable");
      object.removeAttribute("queueExclusive");
    }

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

}
