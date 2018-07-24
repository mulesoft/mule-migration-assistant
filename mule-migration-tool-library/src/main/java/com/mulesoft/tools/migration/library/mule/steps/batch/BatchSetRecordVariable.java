/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;

/**
 * Migrate Batch set record variable component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class BatchSetRecordVariable extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final Namespace CORE_NAMESPACE = Namespace.getNamespace("core", "http://www.mulesoft.org/schema/mule/core");
  public static final String BATCH_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/batch";
  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + BATCH_NAMESPACE_URI + "' and local-name() = 'set-record-variable']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update batch execute to a flow-ref with equal reference name.";
  }

  public BatchSetRecordVariable() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(CORE_NAMESPACE);
    object.setName("set-variable");

    Attribute expression = object.getAttribute("value");
    if (expression != null) {
      String migratedExpression = getExpressionMigrator().migrateExpression(expression.getValue(), true, object);
      migratedExpression = expressionMigrator.wrap(migratedExpression);
      if (migratedExpression.startsWith("#[mel:")) {
        addCompatibilityNamespace(getApplicationModel(), object.getDocument());
      }
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