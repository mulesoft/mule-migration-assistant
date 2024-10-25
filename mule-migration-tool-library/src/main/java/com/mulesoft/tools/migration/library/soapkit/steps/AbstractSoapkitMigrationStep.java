/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.steps;

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
public abstract class AbstractSoapkitMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  static final String SOAPKIT_NS_PREFIX = "apikit-soap";
  static final String SOAPKIT_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/apikit-soap";

  static final Namespace SOAPKIT_NAMESPACE = Namespace.getNamespace(SOAPKIT_NS_PREFIX, SOAPKIT_NAMESPACE_URI);

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
