/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Support for migrating sources of the email connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractEmailSourceMigrator extends AbstractEmailMigrator {

  protected void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, false);

    try {
      addAttributesMapping(getApplicationModel(), getInboundAttributesClass(),
                           inboundToAttributesExpressions(),
                           "message.attributes.headers mapObject ((value, key, index) -> { key : value })");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Map<String, String> inboundToAttributesExpressions() {
    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("toAddresses", "message.attributes.toAddresses joinBy ', '");
    expressionsPerProperty.put("ccAddresses", "message.attributes.ccAddresses joinBy ', '");
    expressionsPerProperty.put("bccAddresses", "message.attributes.bccAddresses joinBy ', '");
    expressionsPerProperty.put("replyToAddresses", "message.attributes.replyToAddresses joinBy ', '");
    expressionsPerProperty.put("fromAddresses", "message.attributes.fromAddresses joinBy ', '");

    expressionsPerProperty.put("subject", "if (message.attributes.subject != '') message.attributes.subject else '(no subject)'");
    expressionsPerProperty.put("contentType", "payload.^mimeType");

    expressionsPerProperty.put("sentDate", "message.attributes.sentDate");
    return expressionsPerProperty;
  }

  protected abstract String getInboundAttributesClass();

}
