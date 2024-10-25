/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Namespace;

/**
 * Common stuff for migrators of APIkit elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractApikitMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  static final String APIKIT_NS_PREFIX = "apikit";
  static final String APIKIT_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/mule-apikit";
  static final String APIKIT_SCHEMA_LOCATION = APIKIT_NAMESPACE_URI + "/current/mule-apikit.xsd";

  static final Namespace APIKIT_NAMESPACE = Namespace.getNamespace(APIKIT_NS_PREFIX, APIKIT_NAMESPACE_URI);

  private ExpressionMigrator expressionMigrator;

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

}
