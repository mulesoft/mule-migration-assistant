/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate EE Cache scope
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CacheScope extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String EE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/core";
  private static final String EE_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd";
  private static final String EE_NAMESPACE_NAME = "ee";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + EE_NAMESPACE_URI + "'"
      + " and local-name()='cache']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate EE Cache scope";
  }

  public CacheScope() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace(EE_NAMESPACE_NAME, EE_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateExpression(element.getAttribute("filterExpression"), expressionMigrator);

    if (element.getAttribute("filter-ref") != null) {
      element.removeAttribute("filter-ref");
      element.setAttribute("filterExpression", "#[false]");
      report.report(ERROR, element, element,
                    "Rewrite the logic of the referenced filter as a DataWeave expression in the 'filterExpression' attribute.");
    }
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }
}
