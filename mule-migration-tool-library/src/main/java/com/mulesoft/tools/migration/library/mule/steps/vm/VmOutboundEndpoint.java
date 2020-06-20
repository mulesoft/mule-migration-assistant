/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmOutboundEndpoint extends AbstractVmEndpoint {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + VM_NAMESPACE_URI + "' and local-name()='outbound-endpoint']";

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
      report.report("vm.nestedTx", tx, object);
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
    while (object.getChild("multi-transaction", CORE_EE_NAMESPACE) != null) {
      Element multiTx = object.getChild("multi-transaction", CORE_EE_NAMESPACE);
      object.setAttribute("transactionalAction",
                          mapTransactionalAction(multiTx.getAttributeValue("action"), report, multiTx, object));

      object.removeChild("multi-transaction", CORE_EE_NAMESPACE);
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
    migrateOutboundVmEndpoint(object, report, connector, configName, vmConfig);

    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true, true);
  }

  public static void migrateOutboundVmEndpoint(Element object, MigrationReport report, Optional<Element> connector,
                                               String configName,
                                               Element vmConfig) {
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
    report.report("vm.sessionVars", content, content);
  }

  public static void migrateVmEndpointConsumer(Element object, MigrationReport report, Optional<Element> connector,
                                               String configName,
                                               Element vmConfig) {
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
  }

}
