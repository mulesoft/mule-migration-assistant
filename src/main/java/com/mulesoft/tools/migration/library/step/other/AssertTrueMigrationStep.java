/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step.other;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.engine.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.engine.step.AbstractMigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * This steps migrates the MUnit 1.x assert-true
 *
 * @author Mulesoft Inc.
 */
public class AssertTrueMigrationStep extends AbstractMigrationStep implements ApplicationModelContribution {

  private static final String XPATH_SELECTOR = "//munit:test/*[contains(local-name(),'true')]";

  private ApplicationModel applicationModel;

  @Override
  public String getDescription() {
    return null;
  }

  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  public void execute() throws Exception {
    try {

      applicationModel.getNodes(XPATH_SELECTOR)
          .forEach(n -> changeNodeName("munit-tools", "assert-that")
              .andThen(changeAttribute("condition", of("expression"), empty()))
              .andThen(addAttribute("is", "#[equalTo(true)]"))
              .apply(n));

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }

}
