/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public abstract class AbstractEmailMigrator extends AbstractApplicationModelMigrationStep {

  public static final Namespace EMAIL_NAMESPACE = Namespace.getNamespace("email", "http://www.mulesoft.org/schema/mule/email");

  protected Optional<Element> resolveConnector(Element object, ApplicationModel appModel) {
    Optional<Element> connector;
    if (object.getAttribute("connector-ref") != null) {
      connector = of(getConnector(object.getAttributeValue("connector-ref")));
      object.removeAttribute("connector-ref");
    } else {
      connector = getDefaultConnector();
    }
    return connector;
  }

  protected abstract Element getConnector(String connectorName);

  protected abstract Optional<Element> getDefaultConnector();

  protected void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, false);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("toAddresses", "message.attributes.toAddresses joinBy ', '");
    expressionsPerProperty.put("ccAddresses", "message.attributes.ccAddresses joinBy ', '");
    expressionsPerProperty.put("bccAddresses", "message.attributes.bccAddresses joinBy ', '");
    expressionsPerProperty.put("replyToAddresses", "message.attributes.replyToAddresses joinBy ', '");
    expressionsPerProperty.put("fromAddresses", "message.attributes.fromAddresses joinBy ', '");

    expressionsPerProperty.put("subject", "if (message.attributes.subject != '') message.attributes.subject else '(no subject)'");
    expressionsPerProperty.put("contentType", "payload.^mimeType");

    expressionsPerProperty.put("sentDate", "message.attributes.sentDate");

    try {
      addAttributesMapping(getApplicationModel(), getInboundAttributesClass(),
                           expressionsPerProperty,
                           "message.attributes.headers mapObject ((value, key, index) -> { key : value })");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract String getInboundAttributesClass();

}
