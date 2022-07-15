/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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

  public static final String WS_NAMESPACE_PREFIX = "ws";
  public static final String WS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ws";
  public static final Namespace WS_NAMESPACE = Namespace.getNamespace(WS_NAMESPACE_PREFIX, WS_NAMESPACE_URI);

  public static final String WSC_NAMESPACE_PREFIX = "wsc";
  public static final String WSC_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/wsc";
  public static final Namespace WSC_NAMESPACE = Namespace.getNamespace(WSC_NAMESPACE_PREFIX, WSC_NAMESPACE_URI);
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

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.runtime.extension.api.soap.SoapAttributes",
                           inboundToAttributesExpressions(),
                           "message.attributes.protocolHeaders");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, String> inboundToAttributesExpressions() {
    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.headers", "message.attributes.protocolHeaders");
    return expressionsPerProperty;
  }


}
