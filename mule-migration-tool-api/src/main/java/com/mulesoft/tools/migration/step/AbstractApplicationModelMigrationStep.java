/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import static com.google.common.base.Preconditions.checkArgument;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;

import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Basic unit of execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */

public abstract class AbstractApplicationModelMigrationStep implements ApplicationModelContribution {

  private static ExpressionMigrator EXPRESSION_MIGRATOR = expr -> {
    return expr.replace("#[", "#[mel:");
  };

  private XPathExpression appliedTo;

  @Override
  public XPathExpression getAppliedTo() {
    return appliedTo;
  }

  @Override
  public void setAppliedTo(String xpathExpression) {
    checkArgument(xpathExpression != null, "The xpath expression must not be null.");
    try {
      this.appliedTo = XPathFactory.instance().compile(xpathExpression);
    } catch (Exception ex) {
      throw new MigrationStepException("The xpath expression must be valid.");
    }
  }

  protected final ExpressionMigrator getExpressionMigrator() {
    return EXPRESSION_MIGRATOR;
  }

}
