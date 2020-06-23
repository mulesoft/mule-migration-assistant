/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import com.mulesoft.tools.migration.library.mule.steps.expression.SimpleExpressionTransformer;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static java.util.Arrays.asList;

/**
 * Migration definition for expression-transformer component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExpressionTransformerMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate expression-transformer component";
  }

  @Override
  public List<MigrationStep> getSteps() {
    return asList(new SimpleExpressionTransformer());
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }
}
