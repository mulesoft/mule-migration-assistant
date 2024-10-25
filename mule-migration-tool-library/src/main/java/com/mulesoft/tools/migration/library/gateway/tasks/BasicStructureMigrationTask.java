/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_POLICY;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static java.util.Collections.singleton;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.AfterExceptionTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.AfterTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.BeforeExceptionTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.BeforeTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.CleanupAttributesMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.DataTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.PointcutTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.PolicyFileRenameMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.PolicyTagMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Basic policy structure migration
 *
 * @author Mulesoft Inc.
 */
public class BasicStructureMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Basic Policy structure migration";
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
    PolicyFileRenameMigrationStep policyFileRenameMigrationStep = new PolicyFileRenameMigrationStep();
    policyFileRenameMigrationStep.setApplicationModel(getApplicationModel());
    List<MigrationStep> steps = new ArrayList<>();
    steps.add(new PolicyTagMigrationStep());
    steps.add(new BeforeTagMigrationStep());
    steps.add(new AfterTagMigrationStep());
    steps.add(new BeforeExceptionTagMigrationStep());
    steps.add(new AfterExceptionTagMigrationStep());
    steps.add(new PointcutTagMigrationStep());
    steps.add(new DataTagMigrationStep());
    steps.add(new CleanupAttributesMigrationStep());
    steps.add(policyFileRenameMigrationStep);
    return steps;
  }
}
