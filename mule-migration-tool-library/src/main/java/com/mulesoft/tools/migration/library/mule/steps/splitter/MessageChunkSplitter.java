/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.SplitterAggregatorUtils.setAggregatorAsProcessed;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Handles migration for 'message-chunk-splitter' and it's matching aggregator.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessageChunkSplitter extends AbstractSplitter {

  private static final String XPATH_SELECTOR =
      "//*[local-name()='message-chunk-splitter' and namespace-uri()='" + CORE_NS_URI + "']";

  public MessageChunkSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  protected String getMatchingAggregatorName() {
    return "message-chunk-aggregator";
  }

  @Override
  public void execute(Element splitter, MigrationReport report) throws RuntimeException {
    report.report("splitter.messageChunk", splitter, splitter);
    getMatchingAggregatorElement(splitter).ifPresent(
                                                     a -> {
                                                       setAggregatorAsProcessed(a);
                                                       report.report("aggregator.messageChunk", a, a);
                                                     });
  }
}
