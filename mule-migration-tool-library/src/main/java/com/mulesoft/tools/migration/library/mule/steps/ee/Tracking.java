/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrate Tracking components expressions
 * <p>
 * DSL for tracking has not changed from Mule 3.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Tracking extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String TRACKING_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/tracking";
  private static final String TRACKING_NAMESPACE_SCHEMA =
      "http://www.mulesoft.org/schema/mule/ee/tracking/current/mule-tracking-ee.xsd";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + TRACKING_NAMESPACE_URI + "'"
      + " and local-name()='meta-data']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate Tracking components expressions";
  }

  public Tracking() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(getNamespace("tracking", TRACKING_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateExpression(element.getAttribute("value"), getExpressionMigrator());
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
