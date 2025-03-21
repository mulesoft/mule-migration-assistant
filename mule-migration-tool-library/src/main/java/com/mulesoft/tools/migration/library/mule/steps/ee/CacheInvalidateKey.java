/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrate EE Cache scope
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CacheInvalidateKey extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + CORE_EE_NS_URI + "'"
      + " and local-name()='invalidate-key']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate EE Cache invalidate-key operation";
  }

  public CacheInvalidateKey() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(CORE_EE_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateExpression(element.getAttribute("keyGenerationExpression"), expressionMigrator);
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
