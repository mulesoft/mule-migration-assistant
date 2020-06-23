/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import com.mulesoft.tools.migration.library.mule.steps.os.OSBasicOperations;
import com.mulesoft.tools.migration.library.mule.steps.os.OSConfig;
import com.mulesoft.tools.migration.library.mule.steps.os.OSDisposeStore;
import com.mulesoft.tools.migration.library.mule.steps.os.OSDualStore;
import com.mulesoft.tools.migration.library.mule.steps.os.OSPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.os.OSRetrieve;
import com.mulesoft.tools.migration.library.mule.steps.os.OSRetrieveStore;
import com.mulesoft.tools.migration.library.mule.steps.os.OSStore;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

/**
 * Migration definition for Object Store connector.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ObjectStoreMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Object Store connector.";
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
    return newArrayList(new OSConfig(),
                        new OSDualStore(),
                        new OSRetrieveStore(),
                        new OSBasicOperations(),
                        new OSStore(),
                        new OSDisposeStore(),
                        new OSRetrieve(),
                        new OSPomContribution());
  }
}
