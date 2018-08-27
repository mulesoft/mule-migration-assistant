/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.validation.CustomValidationMigration;
import com.mulesoft.tools.migration.library.mule.steps.validation.ExceptionFactoryValidationMigration;
import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationAllProcessorMigration;
import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationI18NMigration;
import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration;
import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Task to migrate Validation component.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Validation Component";
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
    return newArrayList(new ValidationMigration(),
                        new CustomValidationMigration(),
                        new ExceptionFactoryValidationMigration(),
                        new ValidationAllProcessorMigration(),
                        new ValidationI18NMigration(),
                        new ValidationPomContribution());
  }
}
