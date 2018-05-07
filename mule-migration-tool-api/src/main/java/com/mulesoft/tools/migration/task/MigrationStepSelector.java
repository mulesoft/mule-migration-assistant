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
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.util.ArrayList;
import java.util.List;

/**
 * It knows how to select a subset of steps.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationStepSelector {

  private List<MigrationStep> steps;

  public MigrationStepSelector(List<MigrationStep> steps) {
    checkArgument(steps != null, "The step list must not be null");
    this.steps = new ArrayList<>(steps);
  }

  public List<NamespaceContribution> getNameSpaceContributionSteps() {
    return (List<NamespaceContribution>) filterAndCast(NamespaceContribution.class);

  }

  public List<ApplicationModelContribution> getApplicationModelContributionSteps() {
    return (List<ApplicationModelContribution>) filterAndCast(ApplicationModelContribution.class);

  }

  public List<ProjectStructureContribution> getProjectStructureContributionSteps() {
    return (List<ProjectStructureContribution>) filterAndCast(ProjectStructureContribution.class);
  }

  public List<PomContribution> getPomContributionSteps() {
    return (List<PomContribution>) filterAndCast(PomContribution.class);
  }

  private List<? extends MigrationStep> filterAndCast(Class<? extends MigrationStep> clazz) {
    return steps.stream()
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .collect(toList());
  }
}
