/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter.COLLECTION_AGGREGATOR;
import static java.util.Optional.of;

import java.util.Optional;

import org.jdom2.Element;

/**
 * Handles migration for 'map-splitter' and it's matching aggregator
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MapSplitter extends AbstractSplitter {

  private static final String XPATH_SELECTOR = "//*[local-name()='map-splitter']";

  private static final String FOR_EACH_EXPRESSION = "#[dw::core::Objects::entrySet(payload)]";

  public MapSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  protected String getMatchingAggregatorName() {
    return COLLECTION_AGGREGATOR;
  }

  @Override
  protected Optional<String> getForEachCollectionAttribute(Element splitterElement) {
    return of(FOR_EACH_EXPRESSION);
  }
}
