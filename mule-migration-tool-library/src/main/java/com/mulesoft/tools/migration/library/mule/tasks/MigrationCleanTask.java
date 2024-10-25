/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.core.GlobalElementsCleanup;
import com.mulesoft.tools.migration.library.mule.steps.core.KeepElementsAtBottomOfFlow;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Task to clean all the behavior added by the migration tool.
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class MigrationCleanTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Clean migration behavior added to the app.";
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
    return newArrayList(new KeepElementsAtBottomOfFlow(),
                        new GlobalElementsCleanup(),
                        new RemoveSyntheticMigrationAttributes(),
                        new RemoveSyntheticMigrationGlobalElements());
  }

}
