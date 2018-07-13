/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;

/**
 * Migration steps for catch exception strategy component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CatchExceptionStrategy extends AbstractExceptionsMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='catch-exception-strategy']";

  @Override
  public String getDescription() {
    return "Update Catch Exception Strategy.";
  }

  public CatchExceptionStrategy() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("", "on-error-continue")
        .apply(element);

    migrateWhenExpression(element);
    encapsulateException(element);
  }
}
