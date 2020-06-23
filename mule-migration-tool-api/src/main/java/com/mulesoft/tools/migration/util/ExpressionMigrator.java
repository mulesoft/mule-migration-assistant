/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
   * Adds the '#[' prefix and ']' suffix to an expression
   * <p>
   * If the expression is already wrapped, it is returned unchanged,
   *
   * @return
   */
  String wrap(String originalExpression);

  /**
   * Checks if the expression has the '#[' prefix and ']' suffix
   *
   * @return
   */
  boolean isWrapped(String originalExpression);

  /**
   * Checks if the expression contains the '#[' prefix and ']' suffix pairs inside
   *
   * @return
   */
  boolean isTemplate(String originalExpression);
}
