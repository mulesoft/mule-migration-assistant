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
 * Migrate EE Cache HTTP caching strategy
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CacheHttpCachingStrategy extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";
  private static final String EE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/core";
  private static final String EE_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd";
  private static final String EE_NAMESPACE_NAME = "ee";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + EE_NAMESPACE_URI + "'"
      + " and local-name()='http-caching-strategy']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate EE Cache HTTP caching strategy";
  }

  public CacheHttpCachingStrategy() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace(EE_NAMESPACE_NAME, EE_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateExpression(element.getAttribute("keyGenerationExpression"), expressionMigrator);

    if (element.getAttribute("consumableFilter-ref") != null) {
      element.removeAttribute("consumableFilter-ref");
      report.report(ERROR, element, element,
                    "'consumableFilter-ref' is not needed in Mule 4 File Connector, since streams are now repeatable and enabled by default.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/streaming-about");
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
