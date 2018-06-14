/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.wsc;

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

  private static final String WSC_NAMESPACE_PREFIX = "wsc";
  private static final String WSC_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/wsc";
  private static final Namespace WSC_NAMESPACE = Namespace.getNamespace(WSC_NAMESPACE_PREFIX, WSC_NAMESPACE_URI);
  public static final String XPATH_SELECTOR = "/mule:mule//ws:consumer";

  @Override
  public String getDescription() {
    return "Update WebService consumer operation.";
  }

  public WsConsumer() {
    this.setAppliedTo(XPATH_SELECTOR);
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
      config = getApplicationModel().getNode("/mule:mule/wsc:config[@name='" + object.getAttributeValue("config-ref") + "']");
    } else {
      config = getApplicationModel().getNode("/mule:mule/wsc:config");
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
