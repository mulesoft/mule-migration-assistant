/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.function.Function;

/**
 * Common stuff for migrators of Assertions elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractAssertionMigration extends AbstractApplicationModelMigrationStep
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


  protected void migrateExpression(Element element) {
    Attribute expression = element.getAttribute("expression");
    if (expression != null) {
      expression.setValue(getExpressionMigrator().migrateExpression(expression.getValue(), true, element));
    }
  }

  protected Function<Element, Element> updateMUnitAssertionEqualsExpression(String attributeName) {
    return e -> {
      Attribute attribute = e.getAttribute(attributeName);
      if (attribute != null) {
        String attributeValue = attribute.getValue();
        if (getExpressionMigrator().isWrapped(attributeValue)) {
          attributeValue = "#[MunitTools::equalTo(" + getExpressionMigrator().unwrap(attributeValue) + ")]";
        } else {
          attributeValue = "#[MunitTools::equalTo(" + attributeValue + ")]";
        }
        attribute.setValue(attributeValue);
      }
      return e;
    };
  }

  protected Function<Element, Element> updateMUnitAssertionNotEqualsExpression(String attributeName) {
    return e -> {
      Attribute attribute = e.getAttribute(attributeName);
      if (attribute != null) {
        String attributeValue = attribute.getValue();
        if (getExpressionMigrator().isWrapped(attributeValue)) {
          attributeValue = "#[MunitTools::not(MUnitTools::equalTo(" + getExpressionMigrator().unwrap(attributeValue) + "))]";
        } else {
          attributeValue = "#[MunitTools::not(MUnitTools::equalTo(" + attributeValue + "))]";
        }
        attribute.setValue(attributeValue);
      }
      return e;
    };
  }
}
