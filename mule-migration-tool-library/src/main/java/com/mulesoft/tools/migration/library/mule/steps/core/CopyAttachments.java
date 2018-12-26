/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

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

  public static final String XPATH_SELECTOR = getCoreXPathSelector("copy-attachments");

  @Override
  public String getDescription() {
    return "Update Copy Attachments.";
  }

  public CopyAttachments() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report("message.inboundAttachments", element, element);
    element.setName("multipart-to-vars");
    element.setNamespace(COMPATIBILITY_NAMESPACE);
    element.getAttribute("attachmentName").setName("partName");
  }
}
