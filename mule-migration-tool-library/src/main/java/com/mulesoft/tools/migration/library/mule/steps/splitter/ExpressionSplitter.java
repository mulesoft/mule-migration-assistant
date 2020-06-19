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
