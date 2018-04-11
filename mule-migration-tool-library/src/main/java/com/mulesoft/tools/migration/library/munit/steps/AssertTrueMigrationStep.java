/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * This steps migrates the MUnit 1.x assert-true
 * @author Mulesoft Inc.
 */
public class AssertTrueMigrationStep extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='assert-true']";

  @Override
  public String getDescription() {
    return null;
  }

  public AssertTrueMigrationStep() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element) throws RuntimeException {
    try {
      changeNodeName("munit-tools", "assert-that")
          .andThen(changeAttribute("condition", of("expression"), empty()))
          .andThen(addAttribute("is", "#[MunitTools::equalTo(true)]"))
          .apply(element);

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }
}
