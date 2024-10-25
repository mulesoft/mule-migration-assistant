/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.VALIDATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.addValidationNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate First Successful
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FirstSuccessful extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("first-successful");

  private ExpressionMigrator expressionMigrator;

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public String getDescription() {
    return "Migrate First Successful.";
  }

  public FirstSuccessful() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    List<Element> childs = new ArrayList<>(element.getChildren());
    childs.forEach(c -> {
      if (c.getName().equals("processor-chain")) {
        c.setName("route");
      } else if (!c.getName().equals("route")) {
        Element route = new Element("route", element.getNamespace());
        Integer index = element.indexOf(c);
        c.detach();
        route.addContent(c);
        element.addContent(index, route);
      }
    });

    if (element.getAttribute("failureExpression") != null) {
      addValidationNamespace(element.getDocument());
      Element validation = new Element("is-false", VALIDATION_NAMESPACE);


      String expression = element.getAttributeValue("failureExpression");
      validation.setAttribute("expression", getExpressionMigrator().migrateExpression(expression, true, element));



      element.getChildren().forEach(c -> c.addContent(c.getContent().size(), validation.clone()));
      element.removeAttribute("failureExpression");
    }
  }
}
