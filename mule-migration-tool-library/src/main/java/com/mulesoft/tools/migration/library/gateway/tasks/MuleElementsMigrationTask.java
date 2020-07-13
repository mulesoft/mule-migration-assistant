/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.gateway.steps.policy.mule.SetPropertyMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.Arrays;
import java.util.List;

/**
 * Policy mule migration task
 *
 * @author Mulesoft Inc.
 */
public class MuleElementsMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Policy mule migration task";
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
  public List<MigrationStep> getSteps() {
    SetPropertyMigrationStep step = new SetPropertyMigrationStep();
    step.setApplicationModel(getApplicationModel());
    return Arrays.asList(step);
  }
}
