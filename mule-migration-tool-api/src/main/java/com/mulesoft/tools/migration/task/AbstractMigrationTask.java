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
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

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
        MigrationStepSelector stepSelector = new MigrationStepSelector(steps);
        if (shouldExecuteAllSteps(stepSelector)) {

          steps.stream().filter(s -> s instanceof ExpressionMigratorAware)
              .forEach(s -> ((ExpressionMigratorAware) s).setExpressionMigrator(getExpressionMigrator()));

          stepSelector.getNameSpaceContributionSteps().forEach(s -> s.execute(applicationModel, report));

          stepSelector.getApplicationModelContributionSteps()
              .forEach(s -> {
                s.setApplicationModel(applicationModel);
                fetchAndProcessNodes(report, s);
              });


          stepSelector.getProjectStructureContributionSteps()
              .forEach(s -> s.execute(applicationModel.getProjectBasePath(), report));

          stepSelector.getPomContributionSteps()
              .forEach(s -> s.execute(applicationModel.getPomModel().orElse(new PomModel()), report));
        }
      }

    } catch (Exception e) {
      throw new MigrationTaskException("Task execution exception. " + e.getMessage(), e);
    }
  }

  private void fetchAndProcessNodes(MigrationReport report, ApplicationModelContribution s) {
    List<Element> nodes = applicationModel.getNodes(s.getAppliedTo());
    nodes.forEach(n -> s.execute(n, report));

    nodes.removeAll(applicationModel.getNodes(s.getAppliedTo()));
    if (!nodes.isEmpty()) {
      // This recursive calls is here so if any task adds nodes to the config that would be processed by this task, those are
      // processed.
      // Also, this is recursive rather than iterative so in the case of a bug, we get a StackOverflow rather than an infinite
      // loop.
      fetchAndProcessNodes(report, s);
    }
  }

  protected boolean shouldExecuteAllSteps(MigrationStepSelector stepSelector) {
    boolean doesNothaveApplicationModelContributions = stepSelector.getApplicationModelContributionSteps().isEmpty();
    boolean isApplicable = stepSelector.getApplicationModelContributionSteps().stream()
        .anyMatch(s -> !applicationModel.getNodes(s.getAppliedTo()).isEmpty());
    return isApplicable || doesNothaveApplicationModelContributions;
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
