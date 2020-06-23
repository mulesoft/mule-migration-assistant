/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.security.properties.SecurePropertiesPlaceholder;
import com.mulesoft.tools.migration.library.mule.steps.security.properties.SecurePropertiesPomContribution;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migrate Secure Properties placeholders
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurePropertiesMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Secure Properties placeholders";
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
    return newArrayList(new SecurePropertiesPomContribution(),
                        new SecurePropertiesPlaceholder());
  }
}
