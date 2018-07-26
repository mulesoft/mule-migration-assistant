/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.List;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;

/**
 * Migrate expressions on Choice router
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ChoiceExpressions extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String XPATH_SELECTOR = "//mule:choice";
  private static final String WHEN_NODE_NAME = "when";
  private static final String EXPRESSION_ATTRIBUTE = "expression";
  private ExpressionMigrator expressionMigrator;

  public ChoiceExpressions() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Migrate Choice expressions.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    List<Element> whenNodes = element.getChildren(WHEN_NODE_NAME, element.getNamespace());
    whenNodes.forEach(this::migrateExpression);
  }

  private void migrateExpression(Element element) {
    Attribute expression = element.getAttribute(EXPRESSION_ATTRIBUTE);
    if (expression != null) {
      String migratedExpression = getExpressionMigrator().migrateExpression(expression.getValue(), true, element);
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
