/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

/**
 * Migrate Set Session Variable to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetSessionVariable extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";

  public static final String XPATH_SELECTOR = "//mule:set-session-variable";
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Set Session Variable namespace to compatibility.";
  }

  public SetSessionVariable() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    XmlDslUtils.migrateExpression(element.getAttribute("value"), getExpressionMigrator());
    report.report(WARN, element, element,
                  "Instead of using session variables in the flow, use variables.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/intro-mule-message#session-properties");
    element.setNamespace(Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE));
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
