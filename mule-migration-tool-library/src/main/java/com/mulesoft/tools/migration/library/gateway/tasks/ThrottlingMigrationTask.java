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

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.DelayResponseTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.DiscardResponseTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.FixedTimeFrameAlgorithmMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.PolicyTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.RateLimitTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.SlaBasedAlgorithmMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.ThrottleTagMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Throttling policy migration task
 *
 * @author Mulesoft Inc.
 */
public class ThrottlingMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Throttling policy migration task";
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
    FixedTimeFrameAlgorithmMigrationStep fixedTimeFrameAlgorithmMigrationStep = new FixedTimeFrameAlgorithmMigrationStep();
    fixedTimeFrameAlgorithmMigrationStep.setApplicationModel(getApplicationModel());
    SlaBasedAlgorithmMigrationStep slaBasedAlgorithmMigrationStep = new SlaBasedAlgorithmMigrationStep();
    slaBasedAlgorithmMigrationStep.setApplicationModel(getApplicationModel());
    List<MigrationStep> steps = new ArrayList<>();
    steps.add(fixedTimeFrameAlgorithmMigrationStep);
    steps.add(slaBasedAlgorithmMigrationStep);
    steps.add(new RateLimitTagMigrationStep());
    steps.add(new DelayResponseTagMigrationStep());
    steps.add(new DiscardResponseTagMigrationStep());
    steps.add(new PolicyTagMigrationStep());
    steps.add(new ThrottleTagMigrationStep());
    return steps;
  }
}
