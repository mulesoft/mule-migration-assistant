/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Set Attachment component to Set Variable
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetAttachment extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='set-attachment']";

  @Override
  public String getDescription() {
    return "Update Set Attachment to Set Variable.";
  }

  public SetAttachment() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report
        .report(ERROR, element, element,
                "Refer to the documentation of the connectors/operations in the flow or the source to know how attachments are sent.",
                "https://beta-migrator.docs-stgx.mulesoft.com/mule4-user-guide/v/4.1/migration-manual#outbound_attachments");

    try {
      changeNodeName("", "set-variable")
          .andThen(changeAttribute("attachmentName", of("variableName"),
                                   of("att_" + element.getAttributeValue("attachmentName"))))
          .andThen(changeAttribute("contentType", of("mimeType"), empty()))
          .apply(element);
    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate Set Attachment.");
    }
  }
}
