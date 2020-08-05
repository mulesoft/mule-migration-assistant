/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import com.mulesoft.tools.migration.library.mule.steps.salesforce.CachedBasicConfiguration;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.CreateOperation;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.QueryAllOperation;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.QueryOperation;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.QuerySingleOperation;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.RetrieveOperation;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.SalesforcePomContribution;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.UpdateOperation;
import com.mulesoft.tools.migration.library.mule.steps.salesforce.UpsertOperation;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

/**
 * Migration definition for Salesforce
 *
 * @author Mulesoft Inc.
 * @since 1.0.1
 */
public class SalesforceMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Salesforce connector operations/configurations";
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
    return newArrayList(
                        new CreateOperation(),
                        new UpdateOperation(),
                        new UpsertOperation(),
                        new RetrieveOperation(),
                        new QueryOperation(),
                        new QuerySingleOperation(),
                        new QueryAllOperation(),
                        new CachedBasicConfiguration(),
                        new SalesforcePomContribution());
  }
}
