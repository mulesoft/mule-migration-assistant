/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;

/**
 * This steps migrates the MUnit 1.x assert-not-null-payload
 * @author Mulesoft Inc.
 */
public class AssertNotNullPayload extends AbstractAssertionMigration {

  public static final String XPATH_SELECTOR = "//*[local-name()='assert-not-null']";

  @Override
  public String getDescription() {
    return "Update Assert Not Null Payload to new MUnit Assertion component";
  }

  public AssertNotNullPayload() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName("munit-tools", "assert-that")
          .andThen(addAttribute("expression", "#[payload]"))
          .andThen(addAttribute("is", "#[MunitTools::notNullValue()]"))
          .apply(element);

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }

}
