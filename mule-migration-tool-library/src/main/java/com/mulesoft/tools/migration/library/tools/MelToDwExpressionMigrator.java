/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools;

import com.mulesoft.tools.Migrator;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Migrate mel expressions to dw expression
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MelToDwExpressionMigrator implements ExpressionMigrator {

  private final Pattern EXPRESSION_WRAPPER = Pattern.compile("^\\s*#\\[(.*)]\\s*$", Pattern.DOTALL);


  @Override
  public String migrateExpression(String originalExpression, boolean dataWeaveBodyOnly) {
    String migratedExpression = Migrator.migrate(unwrap(originalExpression));
    migratedExpression = migratedExpression.replaceAll("flowVars", "vars");
    return dataWeaveBodyOnly ? migratedExpression.replaceFirst("---", "").trim() : migratedExpression;
  }

  @Override
  public String unwrap(String originalExpression) {
    checkExpression(originalExpression);
    Matcher wrappedExpressionMatcher = EXPRESSION_WRAPPER.matcher(originalExpression);
    if (wrappedExpressionMatcher.matches()) {
      return unwrap(wrappedExpressionMatcher.group(1).trim());
    }
    return originalExpression;
  }

  @Override
  public String wrap(String originalExpression) {
    checkExpression(originalExpression);
    return isWrapped(originalExpression) ? originalExpression : "#[" + originalExpression + "]";
  }

  @Override
  public boolean isWrapped(String originalExpression) {
    checkExpression(originalExpression);
    return EXPRESSION_WRAPPER.matcher(originalExpression).matches();
  }

  private void checkExpression(String expression) {
    checkArgument(expression != null, "Expression cannot be null");
  }

}
