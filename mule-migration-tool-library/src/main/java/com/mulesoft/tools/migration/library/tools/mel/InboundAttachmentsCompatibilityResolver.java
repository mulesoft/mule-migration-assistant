/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;

import org.jdom2.Element;

/**
 * Resolver for inbound properties message enrichers
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundAttachmentsCompatibilityResolver implements CompatibilityResolver<String> {

  @Override
  public boolean canResolve(String original) {
    return original != null && original.contains("message.inboundAttachments");
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model) {
    report.report(MigrationReport.Level.ERROR, element, element,
                  "Expressions that use inbound attachments, now should directly use the DataWeave features for handling multipart.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-manual#inbound_attachments");

    return "mel:" + original;
  }
}
