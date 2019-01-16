/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.SplitterAggregatorUtils.isAggregatorProcessed;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Reports aggregators that are not migrated after all splitters were migrated.
 * This can happen because the splitter and the aggregator were not in the same flow. That is a valid configuration that can not be migrated yet, so we should report it.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AggregatorWithNoSplitter extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR =
      "//*[contains(local-name(),'aggregator') and namespace-uri()='" + CORE_NS_URI + "' ]";

  public AggregatorWithNoSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element aggregator, MigrationReport report) throws RuntimeException {
    if (!isAggregatorProcessed(aggregator)) {
      report.report("aggregator.noSplitter", aggregator, aggregator);
    }
  }

}
