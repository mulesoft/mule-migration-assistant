/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate references of exception strategies
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExceptionStrategyRef extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='exception-strategy']";

  @Override
  public String getDescription() {
    return "Update references to Exception Strategies.";
  }

  public ExceptionStrategyRef() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName("", "error-handler")
          .apply(element);
    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate references to Exception Strategies.");
    }
  }
}
