/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addChildNode;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.moveContentToChild;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migration step to update Rollback Exception Strategy
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RollbackExceptionStrategy extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='rollback-exception-strategy']";

  @Override
  public String getDescription() {
    return "Update references to Rollback Exception Strategy.";
  }

  public RollbackExceptionStrategy() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName("", "error-handler")
          .andThen(addChildNode("", "on-error-propagate"))
          .apply(element);
      moveContentToChild("on-error-propagate").apply(element);

      element.getAttribute("maxRedeliveryAttempts").detach();
    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate Rollback Exception Strategy");
    }
  }
}
