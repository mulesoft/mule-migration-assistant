/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Resolver for watermark selector expressions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class WatermarkSelectorMigrator {

  public String migrateSelector(String expression, String selector, Element element, MigrationReport report,
                                ExpressionMigrator expressionMigrator) {
    expression = expression.trim();
    String migratedExpression = expressionMigrator.unwrap(expressionMigrator.migrateExpression(expression, true, element));

    if (migratedExpression.startsWith("mel:")) {
      report.report("watermark.expression", element, element);
    } else {
      switch (selector) {
        case "min":
          migratedExpression = splitSelectorExpression(migratedExpression, "min");
          break;
        case "max":
          migratedExpression = splitSelectorExpression(migratedExpression, "max");
          break;
        case "first":
          migratedExpression = "#[" + migratedExpression + "[0]]";
          break;
        case "last":
          migratedExpression = "#[" + migratedExpression + "[-1]]";
          break;
        default: {
          report.report("watermark.expression", element, element);
          break;
        }
      }
    }
    return migratedExpression;
  }

  private String splitSelectorExpression(String expression, String selector) {
    String leftExpression;
    String rightExpression;

    Integer lastIndDot = expression.lastIndexOf(".");
    if (lastIndDot != -1 && !expression.substring(lastIndDot + 1).endsWith("]")) {
      leftExpression = expression.substring(0, lastIndDot);
      rightExpression = expression.substring(lastIndDot + 1);
    } else {
      leftExpression = expression;
      rightExpression = "";
    }

    return "#[" + selector + "(" + leftExpression + " map $ " + rightExpression + ")]";
  }
}
