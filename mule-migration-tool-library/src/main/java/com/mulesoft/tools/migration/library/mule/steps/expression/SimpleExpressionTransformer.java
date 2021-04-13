/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.expression;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.Optional;
import java.util.StringJoiner;

import org.jdom2.Element;

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

    Optional<String> encoding = ofNullable(object.getAttributeValue("encoding"));
    object.removeAttribute("encoding");

    if (object.getAttribute("mimeType") != null) {
      StringJoiner outputHeader = new StringJoiner(" ").add("output");
      outputHeader.add(object.getAttributeValue("mimeType"));
      object.removeAttribute("mimeType");

      // in DW we can set the output encoding ONLY if we know the mimeType
      encoding.ifPresent(enc -> outputHeader.add(format("encoding='%s'", enc)));
      outputHeader.add("--- ");
      StringBuilder stringBuilder = new StringBuilder(migratedExpression);
      stringBuilder.insert(2, outputHeader.toString());
      migratedExpression = stringBuilder.toString();
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
