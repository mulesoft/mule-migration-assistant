/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.createErrorHandlerParent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isTopLevelElement;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Common stuff for migrators of Exception Handling elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractExceptionsMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private ExpressionMigrator expressionMigrator;

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  protected void migrateWhenExpression(Element element) {
    if (element.getAttribute("when") != null) {
      Attribute whenCondition = element.getAttribute("when");
      whenCondition.setValue(getExpressionMigrator().migrateExpression(whenCondition.getValue(), true, element));
    }
  }

  protected void encapsulateException(Element element) {
    if (!element.getParentElement().getName().equals("error-handler") || isTopLevelElement(element)) {
      createErrorHandlerParent(element);
    }
  }
}
