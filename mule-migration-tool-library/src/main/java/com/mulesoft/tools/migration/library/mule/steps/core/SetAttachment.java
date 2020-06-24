/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
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

  public static final String XPATH_SELECTOR = getCoreXPathSelector("set-attachment");

  @Override
  public String getDescription() {
    return "Update Set Attachment to Set Variable.";
  }

  public SetAttachment() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report("message.outboundAttachments", element, element);

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
