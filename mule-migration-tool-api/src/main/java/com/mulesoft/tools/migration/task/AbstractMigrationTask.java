/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

/**
 * A task is composed by one or more steps
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractMigrationTask implements MigrationTask, ExpressionMigratorAware {

  private ApplicationModel applicationModel;
  private ExpressionMigrator expressionMigrator;

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  @Override
  public void execute(MigrationReport report) throws Exception {
    // TODO depending on the project type this may not be true
    checkState(applicationModel != null, "An application model must be provided.");
    List<MigrationStep> steps = getSteps();
    try {
      if (steps != null) {
        steps.stream().filter(s -> s instanceof ExpressionMigratorAware)
            .forEach(s -> ((ExpressionMigratorAware) s).setExpressionMigrator(getExpressionMigrator()));
        MigrationStepSorter stepSorter = new MigrationStepSorter(steps);

        stepSorter.getNameSpaceContributionSteps().forEach(s -> s.execute(applicationModel, report));

        stepSorter.getApplicationModelContributionSteps()
            .forEach(s -> {
              s.setApplicationModel(applicationModel);
              applicationModel.getNodes(s.getAppliedTo()).forEach(n -> s.execute(n, report));
            });

        stepSorter.getExpressionContributionSteps().forEach(s -> s.execute(new Object(), report));

        stepSorter.getProjectStructureContributionSteps().forEach(s -> s.execute(applicationModel.getProjectBasePath(), report));

        stepSorter.getPomContributionSteps()
            .forEach(s -> s.execute(applicationModel.getPomModel().orElse(new PomModel()), report));
      }

    } catch (Exception e) {
      throw new MigrationTaskException("Task execution exception. " + e.getMessage(), e);
    }
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

}
