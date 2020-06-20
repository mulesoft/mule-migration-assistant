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
package com.mulesoft.tools.migration.library.mule.steps.expression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.Optional;
import java.util.StringJoiner;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

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
