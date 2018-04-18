/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.ExpressionContribution;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.util.ArrayList;
import java.util.List;

/**
 * It knows how to classify a set of steps
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationStepSorter {

  private List<MigrationStep> steps;

  public MigrationStepSorter(List<MigrationStep> steps) {
    checkArgument(steps != null, "The step list must not be null");
    this.steps = new ArrayList<>(steps);
  }

  public List<NamespaceContribution> getNameSpaceContributionSteps() {
    return steps.stream()
        .filter(s -> s instanceof NamespaceContribution)
        .map(s -> (NamespaceContribution) s)
        .collect(toList());
  }

  public List<ApplicationModelContribution> getApplicationModelContributionSteps() {
    return steps.stream()
        .filter(s -> s instanceof ApplicationModelContribution)
        .map(s -> (ApplicationModelContribution) s)
        .collect(toList());
  }

  public List<ExpressionContribution> getExpressionContributionSteps() {
    return steps.stream()
        .filter(s -> s instanceof ExpressionContribution)
        .map(s -> (ExpressionContribution) s)
        .collect(toList());
  }

  public List<ProjectStructureContribution> getProjectStructureContributionSteps() {
    return steps.stream()
        .filter(s -> s instanceof ProjectStructureContribution)
        .map(s -> (ProjectStructureContribution) s)
        .collect(toList());
  }

  public List<PomContribution> getPomContributionSteps() {
    return steps.stream()
        .filter(s -> s instanceof PomContribution)
        .map(s -> (PomContribution) s)
        .collect(toList());
  }


}
