/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter.COLLECTION_AGGREGATOR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.Optional;

import org.jdom2.Element;

/**
 * Handles migration for 'splitter' element along with it's matching aggregator.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExpressionSplitter extends AbstractSplitter implements ExpressionMigratorAware {

  private static final String XPATH_SELECTOR = "//*[local-name()='splitter' and namespace-uri()='" + CORE_NS_URI + "']";

  private static final String OLD_SPLITTER_EVALUATOR_ATTRIBUTE = "evaluator";
  private static final String OLD_SPLITTER_CUSTOM_EVALUATOR_ATTRIUBUTE = "custom-evaluator";

  private ExpressionMigrator expressionMigrator;

  public ExpressionSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return this.expressionMigrator;
  }

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    if (!reportOldAttributesAndFail(splitter, report)) {
      super.execute(splitter, report);
    } else {
      getMatchingAggregatorElement(splitter).ifPresent(SplitterAggregatorUtils::setAggregatorAsProcessed);
    }
  }

  private boolean reportOldAttributesAndFail(Element splitter, MigrationReport report) {
    boolean shouldFail = false;
    if (splitter.getAttributeValue(OLD_SPLITTER_EVALUATOR_ATTRIBUTE) != null) {
      report.report("splitter.evaluatorAttribute", splitter, splitter);
      shouldFail = true;
    }
    if (splitter.getAttributeValue(OLD_SPLITTER_CUSTOM_EVALUATOR_ATTRIUBUTE) != null) {
      report.report("splitter.customEvaluatorAttribute", splitter, splitter);
      shouldFail = true;
    }
    return shouldFail;
  }

  @Override
  protected String getMatchingAggregatorName() {
    return COLLECTION_AGGREGATOR;
  }

  @Override
  protected Optional<String> getForEachCollectionAttribute(Element splitterElement) {
    return of(getExpressionMigrator().migrateExpression(splitterElement.getAttributeValue("expression"), true, splitterElement));

  }
}
