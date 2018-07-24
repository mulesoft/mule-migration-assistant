/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

/**
 * Migrate Validation Module
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationMigration extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String VALIDATION_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/validation";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + VALIDATION_NAMESPACE_URI + "']";
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate Validation Module.";
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  public ValidationMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace("validation", VALIDATION_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateExpression(element.getAttribute("email"), getExpressionMigrator());
    migrateExpression(element.getAttribute("ip"), getExpressionMigrator());
    migrateExpression(element.getAttribute("url"), getExpressionMigrator());
    migrateExpression(element.getAttribute("time"), getExpressionMigrator());
    migrateExpression(element.getAttribute("pattern"), getExpressionMigrator());
    migrateExpression(element.getAttribute("locale"), getExpressionMigrator());
    migrateExpression(element.getAttribute("value"), getExpressionMigrator());
    migrateExpression(element.getAttribute("regex"), getExpressionMigrator());
    migrateExpression(element.getAttribute("caseSensitive"), getExpressionMigrator());
    migrateExpression(element.getAttribute("min"), getExpressionMigrator());
    migrateExpression(element.getAttribute("max"), getExpressionMigrator());
    migrateExpression(element.getAttribute("expression"), getExpressionMigrator());
    migrateExpression(element.getAttribute("minValue"), getExpressionMigrator());
    migrateExpression(element.getAttribute("maxValue"), getExpressionMigrator());
    migrateExpression(element.getAttribute("numberType"), getExpressionMigrator());

    if (element.getName().equals("is-empty")) {
      element.setName("is-empty-collection");
    } else if (element.getName().equals("is-not-empty")) {
      element.setName("is-not-empty-collection");
    }
  }
}
