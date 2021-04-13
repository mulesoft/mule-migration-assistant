/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrate flow-ref components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FlowRef extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("flow-ref");

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate flow-ref components";
  }

  public FlowRef() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (getExpressionMigrator().isWrapped(element.getAttributeValue("name"))) {
      // replace chars in DW
      migrateExpression(element.getAttribute("name"), expressionMigrator);

      String unwrappedExpression = expressionMigrator.unwrap(element.getAttributeValue("name"));
      if (!unwrappedExpression.startsWith("mel:")) {
        element.getAttribute("name").setValue(expressionMigrator.wrap("(" + unwrappedExpression
            + ") replace '/' with '\\\\' replace /\\[|\\{/ with '(' replace /\\]|\\}/ with ')' replace '#' with '_'"));
      }

      report.report("flow.dynamicFlowRefName", element, element);
    } else {
      element.setAttribute("name", element.getAttributeValue("name")
          .replaceAll("/", "\\\\")
          .replaceAll("\\[|\\{", "(")
          .replaceAll("\\]|\\}", ")")
          .replaceAll("#", "_"));
    }
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

}
