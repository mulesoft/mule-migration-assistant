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

import com.mulesoft.tools.migration.engine.step.DefaultMigrationStep;
import org.jdom2.Document;

import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.engine.step.MigrationStep;
import com.mulesoft.tools.migration.engine.step.MigrationStepSorter;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * A task is composed by one or more steps
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class DefaultMigrationTask implements MigrationTask {

  private ApplicationModel applicationModel;

  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  public void execute() throws Exception {
    // TODO depending on the project type this may not be true
    checkState(applicationModel != null, "An application model must be provided.");

    try {
      MigrationStepSorter stepSorter = new MigrationStepSorter(getSteps());

      executeSteps(stepSorter.getNameSpaceContributionSteps());

      stepSorter.getApplicationModelContributionSteps().stream().forEach(s -> s.setApplicationModel(applicationModel));
      executeSteps(stepSorter.getApplicationModelContributionSteps());

      executeSteps(stepSorter.getExpressionContributionSteps());
      executeSteps(stepSorter.getProjectStructureContributionSteps());
      executeSteps(stepSorter.getPomContributionSteps());

    } catch (Exception e) {
      throw new MigrationTaskException("Task execution exception. " + e.getMessage());
    }
  }

  private <T extends MigrationStep> void executeSteps(Set<T> steps) throws MigrationTaskException {
    try {
      for (MigrationStep step : steps) {
        step.execute();
      }
    } catch (Exception ex) {
      // TODO report this failure properly
      throw new MigrationTaskException("Task execution exception. " + ex.getMessage());
    }
  }


  @Deprecated
  public void setDocument(Document document) {
    // this.doc = document;
  }

  @Deprecated
  public void addStep(DefaultMigrationStep step) {
    // if (step != null) {
    // this.migrationSteps.add(step);
    // }
  }
}
