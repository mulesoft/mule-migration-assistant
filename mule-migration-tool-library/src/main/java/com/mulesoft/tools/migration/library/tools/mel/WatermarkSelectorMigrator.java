/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
