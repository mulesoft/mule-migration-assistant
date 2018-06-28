/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;
import static java.lang.Integer.parseInt;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmInboundEndpoint extends AbstractVmEndpoint {

  public static final String XPATH_SELECTOR = "/mule:mule/mule:flow/vm:inbound-endpoint";

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
      report.report(WARN, tx, object,
                    "There can be no transaction active before the listener, so JOIN is not supported at this point.");
      return "ALWAYS_BEGIN";
    } else if ("ALWAYS_JOIN".equals(action)) {
      report.report(WARN, tx, object,
                    "There can be no transaction active before the listener, so JOIN is not supported at this point.");
      return "NONE";
    } else if ("JOIN_IF_POSSIBLE".equals(action)) {
      report.report(WARN, tx, object,
                    "There can be no transaction active before the listener, so JOIN is not supported at this point.");
      return "NONE";
    } else if ("NOT_SUPPORTED".equals(action)) {
      return "NONE";
    }

    return action;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Element tx = object.getChild("transaction", VM_NAMESPACE);
    object.setAttribute("isMessageSource", "true");
    while (tx != null) {
      object.setAttribute("transactionalAction", mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object));
      object.removeChild("transaction", VM_NAMESPACE);
      tx = object.getChild("transaction", VM_NAMESPACE);
    }
    while (object.getChild("xa-transaction", CORE_NAMESPACE) != null) {
      Element xaTx = object.getChild("xa-transaction", CORE_NAMESPACE);
      object.setAttribute("transactionalAction", mapTransactionalAction(xaTx.getAttributeValue("action"), report, xaTx, object));
      object.setAttribute("transactionType", "XA");

      if ("true".equals(xaTx.getAttributeValue("interactWithExternal"))) {
        report.report(ERROR, xaTx, object, "Mule 4 does not support joining with external transactions.");
      }

      object.removeChild("xa-transaction", CORE_NAMESPACE);
    }

    final Namespace vmConnectorNamespace = Namespace.getNamespace("vm", "http://www.mulesoft.org/schema/mule/vm");
    getApplicationModel().addNameSpace(vmConnectorNamespace, "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd",
                                       object.getDocument());

    object.setNamespace(vmConnectorNamespace);
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
        + "VmConfig");

    Optional<Element> config = getApplicationModel().getNodeOptional("mule:mule/vm:config[@name='" + configName + "']");
    Element vmConfig = config.orElseGet(() -> {
      Element vmCfg = new Element("config", vmConnectorNamespace);
      vmCfg.setAttribute("name", configName);
      Element queues = new Element("queues", vmConnectorNamespace);
      vmCfg.addContent(queues);
      // TODO MMT-158
      object.getDocument().getRootElement().addContent(0, vmCfg);

      return vmCfg;
    });

    String path = processAddress(object, report).map(address -> address.getPath()).orElseGet(() -> obtainPath(object));

    addQueue(vmConnectorNamespace, connector, vmConfig, path);

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

    Element content = buildContent(vmConnectorNamespace);
    object.addContent(new Element("response", vmConnectorNamespace).addContent(content));
    report.report(WARN, content, content,
                  "You may remove this if this flow is not using sessionVariables, or after those are migrated to variables.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/intro-mule-message#session-properties");

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("one-way")) {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, false, true);
    } else {
      migrateInboundEndpointStructure(getApplicationModel(), object, report, true, true);
    }
  }

}
