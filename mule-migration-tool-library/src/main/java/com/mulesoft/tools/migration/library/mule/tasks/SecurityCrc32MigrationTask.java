/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.security.crc32.ChecksumPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.security.crc32.Crc32Calculate;
import com.mulesoft.tools.migration.library.mule.steps.security.crc32.Crc32Config;
import com.mulesoft.tools.migration.library.mule.steps.security.crc32.Crc32Filter;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migrate Security Module crc32
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurityCrc32MigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Security crc32";
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
    return newArrayList(new ChecksumPomContribution(),
                        new Crc32Filter(),
                        new Crc32Calculate(),
                        new Crc32Config());
  }
}
