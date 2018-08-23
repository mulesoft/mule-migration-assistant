/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate Batch Step component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class BatchStep extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String BATCH_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/batch";
  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + BATCH_NAMESPACE_URI + "' and local-name() = 'step']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update batch step attributes.";
  }

  public BatchStep() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Attribute acceptPolicy = object.getAttribute("accept-policy");
    if (acceptPolicy != null) {
      acceptPolicy.setName("acceptPolicy");
    }
    Attribute acceptExpression = object.getAttribute("accept-expression");
    if (acceptExpression != null) {
      acceptExpression.setName("acceptExpression");
      acceptExpression.setValue(expressionMigrator.migrateExpression(acceptExpression.getValue(), true, object));
    }
    Attribute filterExpression = object.getAttribute("filter-expression");
    if (filterExpression != null) {
      report
          .report(WARN, object, object,
                  "'filter-expression' does not exist in Mule 4. This may be replaced with 'acceptExpression' attribute in the batch step.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-core-batch#xml_changes");
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
