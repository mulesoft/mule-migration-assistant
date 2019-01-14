/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

public class ExpressionSplitter extends AbstractSplitter {

  private static final String XPATH_SELECTOR = "//*[local-name()='splitter']";

  private static final String OLD_SPLITTER_EVALUATOR_ATTRIBUTE = "evaluator";
  private static final String OLD_SPLITTER_CUSTOM_EVALUATOR_ATTRIUBUTE = "custom-evaluator";

  public ExpressionSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    if (!reportOldAttributesAndFail(splitter, report)) {
      super.execute(splitter, report);
    }
  }

  private boolean reportOldAttributesAndFail(Element splitter, MigrationReport report) {
    boolean shouldFail = false;
    if (splitter.getAttributeValue(OLD_SPLITTER_EVALUATOR_ATTRIBUTE) != null) {
      report.report("splitter.attributes.evaluator", splitter, splitter);
      shouldFail = true;
    }
    if (splitter.getAttributeValue(OLD_SPLITTER_CUSTOM_EVALUATOR_ATTRIUBUTE) != null) {
      report.report("splitter.attributes.customEvaluator", splitter, splitter);
      shouldFail = true;
    }
    return shouldFail;
  }

  @Override
  protected String getMatchingAggregatorName() {
    return "collection-aggregator";
  }

  @Override
  protected void setForEachExpressionAttribute(Element splitterElement, Element forEachElement) {
    forEachElement.setAttribute("expression",
                                getExpressionMigrator().migrateExpression(
                                                                          splitterElement.getAttributeValue("expression"),
                                                                          true,
                                                                          splitterElement));
  }
}
