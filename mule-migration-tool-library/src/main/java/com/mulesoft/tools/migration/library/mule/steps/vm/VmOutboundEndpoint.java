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
 * Migrates the inbound endpoint of the VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmOutboundEndpoint extends AbstractVmEndpoint {

  public static final String XPATH_SELECTOR = "//vm:outbound-endpoint";

  @Override
  public String getDescription() {
    return "Update VM transport outbound endpoint.";
  }

  public VmOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(VM_NAMESPACE));
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
    Element tx = object.getChild("transaction", VM_NAMESPACE);
    while (tx != null) {
      object.setAttribute("transactionalAction", mapTransactionalAction(tx.getAttributeValue("action"), report, tx, object));
      object.removeChild("transaction", VM_NAMESPACE);
      tx = object.getChild("transaction", VM_NAMESPACE);
    }
    while (object.getChild("xa-transaction", CORE_NAMESPACE) != null) {
      Element xaTx = object.getChild("xa-transaction", CORE_NAMESPACE);
      object.setAttribute("transactionalAction", mapTransactionalAction(xaTx.getAttributeValue("action"), report, xaTx, object));

      object.removeChild("xa-transaction", CORE_NAMESPACE);
    }

    getApplicationModel().addNameSpace(VM_NAMESPACE, "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd",
                                       object.getDocument());
    object.setNamespace(VM_NAMESPACE);

    if (object.getAttribute("exchange-pattern") == null
        || object.getAttributeValue("exchange-pattern").equals("one-way")) {
      object.setName("publish");
    } else {
      object.setName("publish-consume");
    }

    Optional<Element> connector = resolveVmConector(object, getApplicationModel());
    String configName = getVmConfigName(object, connector);
    Element vmConfig = migrateVmConfig(object, connector, configName, getApplicationModel());
    String path = processAddress(object, report).map(address -> address.getPath()).orElseGet(() -> obtainPath(object));

    addQueue(VM_NAMESPACE, connector, vmConfig, path);

    if (object.getAttribute("responseTimeout") != null) {
      object.setAttribute("timeout", object.getAttributeValue("responseTimeout"));
      object.setAttribute("timeoutUnit", "MILLISECONDS");
      object.removeAttribute("responseTimeout");
    }

    object.setAttribute("config-ref", configName);
    object.setAttribute("queueName", path);
    object.removeAttribute("path");
    object.removeAttribute("name");
    object.removeAttribute("mimeType");
    object.removeAttribute("disableTransportTransformer");

    Element content = buildContent(VM_NAMESPACE);
    object.addContent(content);
    report.report(WARN, content, content,
                  "You may remove this if this flow is not using sessionVariables, or after those are migrated to variables.",
                  "https://beta-migrator.docs-stgx.mulesoft.com/mule4-user-guide/v/4.1/migration-manual#session_variables");

    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true, true);
  }

}
