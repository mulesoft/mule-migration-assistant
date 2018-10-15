/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

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
    changeNodeName("", "error-handler")
        .apply(element);

    if (element.getParentElement().getName().equals("error-handler")) {
      report.report("errorHandling.reuse", element, element);
    }
  }
}
