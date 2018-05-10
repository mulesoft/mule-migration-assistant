/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import com.mulesoft.tools.migration.step.category.ExpressionMigrator;


/**
 * Element that can has an expression migrator. All steps that use an expression migrator should implement this interface.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ExpressionMigratorAware {

  void setExpressionMigrator(ExpressionMigrator expressionMigrator);

  ExpressionMigrator getExpressionMigrator();
}
