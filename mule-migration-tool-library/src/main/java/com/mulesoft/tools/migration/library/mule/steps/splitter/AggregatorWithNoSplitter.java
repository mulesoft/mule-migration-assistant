/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

public class AggregatorWithNoSplitter extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR = "//mule:*[contains(local-name(),'aggregator')]";

  public AggregatorWithNoSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element aggregator, MigrationReport report) throws RuntimeException {
    report.report("splitter.aggregator.noSplitter", aggregator, aggregator);
  }

}
