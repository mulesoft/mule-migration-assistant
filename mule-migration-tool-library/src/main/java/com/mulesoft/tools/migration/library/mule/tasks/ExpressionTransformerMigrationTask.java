/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
