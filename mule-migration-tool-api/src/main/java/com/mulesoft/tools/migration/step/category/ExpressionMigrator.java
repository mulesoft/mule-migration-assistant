/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

/**
 * An interface to handle the migration of expressions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ExpressionMigrator {

  /**
   * Converts a Mule 3 expression to its Mule 4 equivalent.
   *
   * @return an expression that works in Mule 4
   */
  String migrateExpression(String originalExpression);
}
