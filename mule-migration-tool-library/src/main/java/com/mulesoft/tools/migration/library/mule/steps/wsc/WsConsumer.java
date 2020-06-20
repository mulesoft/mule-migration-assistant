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
package com.mulesoft.tools.migration.library.mule.steps.wsc;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Migrates the configuration of the WebService consumer operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class WsConsumer extends AbstractApplicationModelMigrationStep {

  private static final String WS_NAMESPACE_PREFIX = "ws";
  public static final String WS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ws";
  public static final Namespace WS_NAMESPACE = Namespace.getNamespace(WS_NAMESPACE_PREFIX, WS_NAMESPACE_URI);

  private static final String WSC_NAMESPACE_PREFIX = "wsc";
  private static final String WSC_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/wsc";
  private static final Namespace WSC_NAMESPACE = Namespace.getNamespace(WSC_NAMESPACE_PREFIX, WSC_NAMESPACE_URI);
  public static final String XPATH_SELECTOR = "//ws:consumer";

  @Override
  public String getDescription() {
    return "Update WebService consumer operation.";
  }

  public WsConsumer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(WS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {

    getApplicationModel().addNameSpace(WSC_NAMESPACE_PREFIX, WSC_NAMESPACE_URI,
                                       "http://www.mulesoft.org/schema/mule/wsc/current/mule-wsc.xsd");
    object.setNamespace(WSC_NAMESPACE);
    object.setName("consume");

    addAttributesToInboundProperties(object, report);

    Element config;
    if (object.getAttribute("config-ref") != null) {
      config = getApplicationModel().getNode("/*/wsc:config[@name='" + object.getAttributeValue("config-ref") + "']");
    } else {
      config = getApplicationModel().getNode("/*/wsc:config");
      object.setAttribute("config-ref", config.getAttributeValue("name"));
    }

    // TODO MMT-122 Anything else to do regarding attachments/mtom? check
    // org.mule.module.ws.consumer.WSConsumer.copyAttachmentsRequest(MuleEvent) in 3.x

    if (object.getAttribute("mtomEnabled") != null) {
      copyAttributeIfPresent(object, config.getChild("connection", WSC_NAMESPACE), "mtomEnabled");
      object.removeAttribute("mtomEnabled");
    }
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateOperationStructure(getApplicationModel(), object, report);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.headers", "message.attributes.protocolHeaders");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.runtime.extension.api.soap.SoapAttributes", expressionsPerProperty,
                           "message.attributes.protocolHeaders");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
