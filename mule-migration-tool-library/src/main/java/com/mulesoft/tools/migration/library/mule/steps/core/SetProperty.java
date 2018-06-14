/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate Set Property to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetProperty extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String COMPATIBILITY_NAMESPACE_PREFIX = "compatibility";
  private static final String COMPATIBILITY_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/compatibility";
  private static final Namespace COMPATIBILITY_NAMESPACE =
      Namespace.getNamespace(COMPATIBILITY_NAMESPACE_PREFIX, COMPATIBILITY_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//mule:set-property";
  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Set Property namespace to compatibility.";
  }

  public SetProperty() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(COMPATIBILITY_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    XmlDslUtils.migrateExpression(element.getAttribute("value"), getExpressionMigrator());
    report.report(WARN, element, element,
                  "Instead of setting outbound properties in the flow, its values must be set explicitly in the operation/listener.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
    element.setNamespace(COMPATIBILITY_NAMESPACE);
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
