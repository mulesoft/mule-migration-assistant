/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
