/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.FederationPomContributionMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.Oauth2GwValidateTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.OpenAmGwValidateTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.OpenIdConnectGwValidateTagMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.federation.PingFederateGwValidateTagMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.Arrays;
import java.util.List;

/**
 * Federation migration task
 *
 * @author Mulesoft Inc.
 */
public class FederationMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Federation migration task";
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
    return Arrays.asList(
                         new FederationPomContributionMigrationStep(),
                         new Oauth2GwValidateTagMigrationStep(),
                         new OpenAmGwValidateTagMigrationStep(),
                         new OpenIdConnectGwValidateTagMigrationStep(),
                         new PingFederateGwValidateTagMigrationStep());
  }
}
