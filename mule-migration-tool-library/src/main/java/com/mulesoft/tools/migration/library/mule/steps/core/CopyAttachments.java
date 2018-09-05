/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Copy Attachments component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CopyAttachments extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='copy-attachments']";

  @Override
  public String getDescription() {
    return "Update Copy Attachments.";
  }

  public CopyAttachments() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report(WARN, element, element, "Identify the received attachments and set them as variables.",
                  "https://beta-migrator.docs-stgx.mulesoft.com/mule4-user-guide/v/4.1/migration-manual#inbound_attachments");
    element.setName("multipart-to-vars");
    element.setNamespace(COMPATIBILITY_NAMESPACE);
    element.getAttribute("attachmentName").setName("partName");
  }
}
