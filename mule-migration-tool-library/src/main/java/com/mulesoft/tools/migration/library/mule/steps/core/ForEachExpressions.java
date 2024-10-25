/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate expressions on For Each router
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ForEachExpressions extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String XPATH_SELECTOR = "//mule:foreach";
  private static final String EXPRESSION_ATTRIBUTE = "collection";
  private ExpressionMigrator expressionMigrator;

  public ForEachExpressions() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Migrate For Each expressions.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Attribute expression = element.getAttribute(EXPRESSION_ATTRIBUTE);
    if (expression != null) {
      String migratedExpression = expressionMigrator.migrateExpression(expression.getValue(), true, element);
      migratedExpression = expressionMigrator.wrap(migratedExpression);
      expression.setValue(migratedExpression);
    }
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
