/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter.COLLECTION_AGGREGATOR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
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

  private static final String XPATH_SELECTOR = "//*[local-name()='map-splitter' and namespace-uri()='" + CORE_NS_URI + "']";

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
