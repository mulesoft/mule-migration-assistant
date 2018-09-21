/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.util;

import org.jdom2.Element;

/**
 * An interface to handle the migration of expressions.
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
  String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element);

  /**
   * Converts a Mule 3 expression to its Mule 4 equivalent.
   *
   * @return an expression that works in Mule 4
   */
  default String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly, Element element, boolean enricher) {
    return migrateExpression(originalExpression, dataWeaveBodyOnly, element);
  }

  /**
   * Removes the '#[' prefix and ']' suffix from an expression.
   * <p>
   * If the expression is not wrapped, it is returned unchanged,
   *
   * @return
   */
  String unwrap(String originalExpression);

  /**
   * Adds the  '#[' prefix and ']' suffix to an expression
   * <p>
   * If the expression is already wrapped, it is returned unchanged,
   *
   * @return
   */
  String wrap(String originalExpression);

  /**
   * Checks if the expression has the  '#[' prefix and ']' suffix
   *
   * @return
   */
  boolean isWrapped(String originalExpression);
}
