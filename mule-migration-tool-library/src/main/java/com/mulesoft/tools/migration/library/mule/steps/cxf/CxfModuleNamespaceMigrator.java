/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.cxf;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Changes the namespace of the elements of the CXF module
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CxfModuleNamespaceMigrator extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='http://www.mulesoft.org/schema/mule/cxf']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update CXF module config.";
  }

  public CxfModuleNamespaceMigrator() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(Namespace.getNamespace("cxf", "http://www.mulesoft.org/schema/mule/cxf"), XPATH_SELECTOR,
                                       null);
    object.setNamespace(Namespace.getNamespace("cxf", "http://www.mulesoft.org/schema/mule/cxf"));

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
