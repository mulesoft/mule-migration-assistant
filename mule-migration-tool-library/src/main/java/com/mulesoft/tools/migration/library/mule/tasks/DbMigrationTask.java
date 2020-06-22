/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.db.DbConfig;
import com.mulesoft.tools.migration.library.mule.steps.db.DbConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.db.DbDdlExecute;
import com.mulesoft.tools.migration.library.mule.steps.db.DbDelete;
import com.mulesoft.tools.migration.library.mule.steps.db.DbExecute;
import com.mulesoft.tools.migration.library.mule.steps.db.DbInsert;
import com.mulesoft.tools.migration.library.mule.steps.db.DbSelect;
import com.mulesoft.tools.migration.library.mule.steps.db.DbStoredProcedure;
import com.mulesoft.tools.migration.library.mule.steps.db.DbUpdate;
import com.mulesoft.tools.migration.library.mule.steps.db.JbossTxManager;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate DB Connector";
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
    return newArrayList(new DbConnectorPomContribution(),
                        new DbConfig(),
                        new DbSelect(),
                        new DbInsert(),
                        new DbUpdate(),
                        new DbDelete(),
                        new DbExecute(),
                        new DbStoredProcedure(),
                        new DbDdlExecute(),
                        new JbossTxManager());
  }
}
