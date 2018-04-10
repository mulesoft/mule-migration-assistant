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

  private static ExpressionMigrator EXPRESSION_MIGRATOR = new ExpressionMigrator() {

    @Override
    public String unwrap(String originalExpression) {
      if (isWrapped(originalExpression)) {
        return originalExpression.substring(2, originalExpression.length() - 1);
      } else {
        return originalExpression;
      }
    }

    @Override
    public String wrap(String originalExpression) {
      if (isWrapped(originalExpression)) {
        return originalExpression;
      } else {
        return "#[" + originalExpression + "]";
      }
    }

    public boolean isWrapped(String originalExpression) {
      return originalExpression.startsWith("#[") && originalExpression.endsWith("]");
    };

    @Override
    public String migrateExpression(String originalExpression) {
      // TODO
      return originalExpression.replace("#[", "#[mel:");
    }
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
