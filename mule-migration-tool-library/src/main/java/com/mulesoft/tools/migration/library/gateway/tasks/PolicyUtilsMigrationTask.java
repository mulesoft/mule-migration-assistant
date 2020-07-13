/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_POLICY;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static java.util.Collections.singleton;

import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.PolicyDeployPropertiesPomContributionMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.PolicyYamlMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Policy utils migration task
 *
 * @author Mulesoft Inc.
 */
public class PolicyUtilsMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Policy utils migration task";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public Set<ProjectType> getApplicableProjectTypes() {
    return singleton(MULE_FOUR_POLICY);
  }

  @Override
  public List<MigrationStep> getSteps() {
    return Arrays.asList(new PolicyYamlMigrationStep(), new PolicyDeployPropertiesPomContributionMigrationStep());
  }
}
