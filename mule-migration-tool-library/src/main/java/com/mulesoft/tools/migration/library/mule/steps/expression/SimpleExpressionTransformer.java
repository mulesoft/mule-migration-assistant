/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.expression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Migrate <expression-transformer expression="" /> to <set-payload value=#[expression] />.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SimpleExpressionTransformer extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//mule:expression-transformer[not(@evaluator) and not(@returnClass)]";
  private ExpressionMigrator expressionMigrator;

  public SimpleExpressionTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("set-payload");
    String migratedExpression;

    if (object.getChildren().isEmpty()) {
      migratedExpression = getElementExpressionValue(object).orElse("#[]");
      object.removeAttribute("expression");
    } else if (object.getChildren().size() == 1 && object.getChildren().get(0).getName().equals("return-argument")) {
      Element returnArgument = object.getChildren().get(0);
      migratedExpression = getElementExpressionValue(returnArgument).orElse("#[]");
      returnArgument.detach();
    } else {
      report.report("expressionTransformer.multipleTransforms", object, object);
      return;
    }


    if (object.getAttribute("mimeType") != null) {
      StringBuilder stringBuilder = new StringBuilder(migratedExpression);
      stringBuilder.insert(2, format("output %s --- ", object.getAttributeValue("mimeType")));
      migratedExpression = stringBuilder.toString();
      object.removeAttribute("mimeType");
    }

    object.setAttribute("value", migratedExpression);
  }

  private Optional<String> getElementExpressionValue(Element element) {
    if (element.getAttribute("expression") != null) {
      String currentExpression = getExpressionMigrator().wrap(element.getAttributeValue("expression"));
      String migratedExpression = getExpressionMigrator().migrateExpression(currentExpression, true, element);

      return of(migratedExpression);
    }
    return empty();
  }

}
