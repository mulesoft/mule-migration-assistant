/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.createErrorHandlerParent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isTopLevelElement;

/**
 * Migration steps for catch exception strategy component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CatchExceptionStrategy extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//*[local-name()='catch-exception-strategy']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Catch Exception Strategy.";
  }

  public CatchExceptionStrategy() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("", "on-error-continue")
        .apply(element);

    if (element.getAttribute("when") != null) {
      Attribute whenCondition = element.getAttribute("when");
      whenCondition.setValue(getExpressionMigrator().migrateExpression(whenCondition.getValue(), true, element));
    }

    if (!element.getParentElement().getName().equals("error-handler") || isTopLevelElement(element)) {
      createErrorHandlerParent(element);
    }
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
