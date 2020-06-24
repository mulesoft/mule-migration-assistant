/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import com.mulesoft.tools.migration.util.ExpressionMigrator;


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
