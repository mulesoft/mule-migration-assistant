/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter.COLLECTION_AGGREGATOR;
import static com.mulesoft.tools.migration.library.mule.steps.splitter.SplitterAggregatorUtils.setAggregatorAsProcessed;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Handles migration for 'custom-splitter' and matching aggregator.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CustomSplitter extends AbstractSplitter {

  private static final String XPATH_SELECTOR = "//*[local-name()='custom-splitter' and namespace-uri()='" + CORE_NS_URI + "']";

  public CustomSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    report.report("splitter.custom", splitter, splitter);
    getMatchingAggregatorElement(splitter).ifPresent(
                                                     a -> {
                                                       setAggregatorAsProcessed(a);
                                                       report.report("aggregator.customSplitter", a, a);
                                                     });
  }

  @Override
  protected String getMatchingAggregatorName() {
    return COLLECTION_AGGREGATOR;
  }
}
