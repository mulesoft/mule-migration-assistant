/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import static com.mulesoft.tools.migration.library.mule.steps.batch.BatchJob.BATCH_NAMESPACE_URI;

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
      report.report("batch.filterExpression", object, object);
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
