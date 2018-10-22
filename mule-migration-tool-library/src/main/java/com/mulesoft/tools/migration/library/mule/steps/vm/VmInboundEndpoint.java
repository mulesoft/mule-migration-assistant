/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleConnectorChildElements;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;
import static java.lang.Integer.parseInt;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmInboundEndpoint extends AbstractVmEndpoint {

  public static final String XPATH_SELECTOR = "/*/mule:flow/vm:inbound-endpoint";

  @Override
  public String getDescription() {
    return "Update VM transport inbound endpoint.";
  }

  public VmInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(VM_NAMESPACE));
  }

  private String mapTransactionalAction(String action, MigrationReport report, Element tx, Element object) {
    // Values defined in org.mule.runtime.extension.api.tx.SourceTransactionalAction
    if ("BEGIN_OR_JOIN".equals(action)) {
      report.report("vm.listenerTx", tx, object);
      return "ALWAYS_BEGIN";
    } else if ("ALWAYS_JOIN".equals(action)) {
      report.report("vm.listenerTx", tx, object);
      return "NONE";
    } else if ("JOIN_IF_POSSIBLE".equals(action)) {
      report.report("vm.listenerTx", tx, object);
      return "NONE";
    } else if ("NOT_SUPPORTED".equals(action)) {
      return "NONE";
    }

    return action;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    Element tx = object.getChild("transaction", VM_NAMESPACE);
    while (tx != null) {
      String txAction = mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object);
      object.setAttribute("transactionalAction", txAction);
      if (!"NONE".equals(txAction)) {
        if (object.getChild("redelivery-policy", CORE_NAMESPACE) == null) {
          object.addContent(new Element("redelivery-policy", CORE_NAMESPACE));
        }
      }
      object.removeChild("transaction", VM_NAMESPACE);
      tx = object.getChild("transaction", VM_NAMESPACE);
    }
    while (object.getChild("xa-transaction", CORE_NAMESPACE) != null) {
      Element xaTx = object.getChild("xa-transaction", CORE_NAMESPACE);
      String txAction = mapTransactionalAction(xaTx.getAttributeValue("action"), report, xaTx, object);
      object.setAttribute("transactionalAction", txAction);
      object.setAttribute("transactionType", "XA");
      if (!"NONE".equals(txAction)) {
        if (object.getChild("redelivery-policy", CORE_NAMESPACE) == null) {
          object.addContent(new Element("redelivery-policy", CORE_NAMESPACE));
        }
      }

      if ("true".equals(xaTx.getAttributeValue("interactWithExternal"))) {
        report.report("vm.externalTx", xaTx, object);
      }

      object.removeChild("xa-transaction", CORE_NAMESPACE);
    }
    while (object.getChild("multi-transaction", CORE_EE_NAMESPACE) != null) {
      Element multiTx = object.getChild("multi-transaction", CORE_EE_NAMESPACE);
      String txAction = mapTransactionalAction(multiTx.getAttributeValue("action"), report, multiTx, object);
      object.setAttribute("transactionalAction", txAction);
      if (!"NONE".equals(txAction)) {
        if (object.getChild("redelivery-policy", CORE_NAMESPACE) == null) {
          object.addContent(new Element("redelivery-policy", CORE_NAMESPACE));
        }
      }

      object.removeChild("multi-transaction", CORE_EE_NAMESPACE);
    }

    getApplicationModel().addNameSpace(VM_NAMESPACE, "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd",
                                       object.getDocument());

    object.setNamespace(VM_NAMESPACE);
    object.setName("listener");

    Optional<Element> connector = resolveVmConector(object, getApplicationModel());
    String configName = getVmConfigName(object, connector);
    Element vmConfig = migrateVmConfig(object, connector, configName, getApplicationModel());
    String path = processAddress(object, report).map(address -> address.getPath()).orElseGet(() -> obtainPath(object));

    addQueue(VM_NAMESPACE, connector, vmConfig, path);

    connector.ifPresent(conn -> {
      Integer consumers = null;
      if (conn.getAttribute("numberOfConcurrentTransactedReceivers") != null) {
        consumers = parseInt(conn.getAttributeValue("numberOfConcurrentTransactedReceivers"));
      } else if (conn.getChild("receiver-threading-profile", CORE_NAMESPACE) != null
          && conn.getChild("receiver-threading-profile", CORE_NAMESPACE).getAttribute("maxThreadsActive") != null) {
        consumers = parseInt(conn.getChild("receiver-threading-profile", CORE_NAMESPACE).getAttributeValue("maxThreadsActive"));
      }

      if (consumers != null) {
        getFlow(object).setAttribute("maxConcurrency", "" + consumers);
        object.setAttribute("numberOfConsumers", "" + consumers);
      }

      handleConnectorChildElements(conn, vmConfig, new Element("connection", CORE_NAMESPACE), report);
    });

    if (object.getAttribute("mimeType") != null) {
      Element setMimeType =
          new Element("set-payload", CORE_NAMESPACE)
              .setAttribute("value", "#[output " + object.getAttributeValue("mimeType") + " --- payload]");

      addElementAfter(setMimeType, object);
      object.removeAttribute("mimeType");
    }

    if (object.getAttribute("responseTimeout") != null) {
      object.setAttribute("timeout", object.getAttributeValue("responseTimeout"));
      object.setAttribute("timeoutUnit", "MILLISECONDS");
      object.removeAttribute("responseTimeout");
    }

    object.setAttribute("config-ref", configName);
    object.setAttribute("queueName", path);
    object.removeAttribute("path");
    object.removeAttribute("name");
    object.removeAttribute("disableTransportTransformer");

    Element content = buildContent(VM_NAMESPACE);
    object.addContent(new Element("response", VM_NAMESPACE).addContent(content));
    report.report("vm.sessionVars", content, content);

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("one-way")) {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, false, true);
    } else {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, true, true);
    }
  }

}
