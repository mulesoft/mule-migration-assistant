/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.task;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.engine.step.MigrationStep;
import com.mulesoft.tools.migration.engine.step.MigrationStepSorter;
import com.mulesoft.tools.migration.engine.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.pom.PomModel;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

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

        stepSorter.getNameSpaceContributionSteps().forEach(s -> s.setApplicationModel(applicationModel));
        executeSteps(stepSorter.getNameSpaceContributionSteps());

        executeSteps(stepSorter.getApplicationModelContributionSteps());

        executeSteps(stepSorter.getExpressionContributionSteps());
        executeSteps(stepSorter.getProjectStructureContributionSteps());

        stepSorter.getPomContributionSteps().forEach(s -> s.setPomModel(applicationModel.getPomModel().orElse(new PomModel())));
        executeSteps(stepSorter.getPomContributionSteps());
      }

    } catch (Exception e) {
      throw new MigrationTaskException("Task execution exception. " + e.getMessage());
    }
  }

  private <T extends MigrationStep> void executeSteps(Set<T> steps) throws MigrationStepException {
    try {
      for (MigrationStep step : steps) {
        if (step instanceof ApplicationModelContribution) {
          applicationModel.getNodes(step.getAppliedTo()).forEach(s -> {
            try {
              step.setElement(s);
              step.execute();
            } catch (Exception ex) {
              throw new MigrationStepException("Step execution exception. " + ex.getMessage());
            }
          });
        } else {
          step.execute();
        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Step execution exception. " + ex.getMessage());
    }
  }

}
