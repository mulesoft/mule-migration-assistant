/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
