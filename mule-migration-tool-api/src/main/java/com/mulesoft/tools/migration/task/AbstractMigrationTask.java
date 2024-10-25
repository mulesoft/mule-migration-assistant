/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.exception.MigrationAbortException;
import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.ReportingStep;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A task is composed by one or more steps
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractMigrationTask implements MigrationTask, ExpressionMigratorAware {

  private ApplicationModel applicationModel;
  private ExpressionMigrator expressionMigrator;

  private XMLOutputter outp = new XMLOutputter();

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
    List<MigrationStep> steps = enableReporting(getSteps());

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
                fetchAndProcessNodes(report, s, new ArrayList<>());
              });


          stepSelector.getProjectStructureContributionSteps()
              .forEach(s -> {
                s.setApplicationModel(applicationModel);
                s.execute(applicationModel.getProjectBasePath(), report);
              });

          stepSelector.getPomContributionSteps()
              .forEach(s -> {
                s.setApplicationModel(applicationModel);
                s.execute(applicationModel.getPomModel().orElse(new PomModel()), report);
              });
        }
      }

    } catch (MigrationAbortException e) {
      throw e;
    } catch (Exception e) {
      throw new MigrationTaskException("Task execution exception. " + e.getMessage(), e);
    }
  }

  private List<MigrationStep> enableReporting(List<MigrationStep> steps) {
    if (steps != null) {
      return steps.stream().map(step -> {
        if (step instanceof ApplicationModelContribution) {
          return new ReportingStep((ApplicationModelContribution) step);
        }
        return step;
      }).collect(Collectors.toList());
    }
    return null;
  }

  private void fetchAndProcessNodes(MigrationReport report, ApplicationModelContribution s, List<Element> alreadyProcessed) {
    AtomicInteger processedElements = new AtomicInteger(0);

    List<Element> nodes = applicationModel.getNodes(s.getAppliedTo());
    nodes.stream().filter(n -> !alreadyProcessed.contains(n)).forEach(n -> {
      try {
        processedElements.incrementAndGet();
        s.execute(n, report);
      } catch (Exception e) {
        throw new MigrationStepException("Task execution exception (" + e.getMessage() + ") migrating node:" + lineSeparator()
            + outp.outputString(n), e);
      }
    });

    alreadyProcessed.addAll(nodes);

    nodes.removeAll(applicationModel.getNodes(s.getAppliedTo()));
    if (!nodes.isEmpty()) {
      // This recursive calls is here so if any task adds nodes to the config that would be processed by this task, those are
      // processed.
      // Also, this is recursive rather than iterative so in the case of a bug, we get a StackOverflow rather than an infinite
      // loop.
      fetchAndProcessNodes(report, s, alreadyProcessed);
    }

    report.addProcessedElements(processedElements.get());
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
