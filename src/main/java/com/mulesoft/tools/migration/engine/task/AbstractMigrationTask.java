/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.task;

import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.engine.step.MigrationStepSorter;
import com.mulesoft.tools.migration.pom.PomModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A task is composed by one or more steps
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractMigrationTask implements MigrationTask {

  private ApplicationModel applicationModel;

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  public void execute() throws Exception {
    // TODO depending on the project type this may not be true
    checkState(applicationModel != null, "An application model must be provided.");

    try {
      if (getSteps() != null) {
        MigrationStepSorter stepSorter = new MigrationStepSorter(getSteps());

        stepSorter.getNameSpaceContributionSteps().forEach(s -> s.execute(applicationModel));

        stepSorter.getApplicationModelContributionSteps().stream().forEach(s -> applicationModel.getNodes(s.getAppliedTo()).forEach(s::execute));

        stepSorter.getExpressionContributionSteps().forEach(s -> s.execute(new Object()));
        stepSorter.getProjectStructureContributionSteps().forEach(s -> s.execute(new Object()));

        stepSorter.getPomContributionSteps().forEach(s -> s.execute(applicationModel.getPomModel().orElse(new PomModel())));

      }

    } catch (Exception e) {
      throw new MigrationTaskException("Task execution exception. " + e.getMessage());
    }
  }

}
