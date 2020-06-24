/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;

/**
 * Migrates 'collection-splitter' along with it's matching aggregator.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CollectionSplitter extends AbstractSplitter {

  public static final String COLLECTION_AGGREGATOR = "collection-aggregator";

  private static final String XPATH_SELECTOR =
      "//*[local-name()='collection-splitter' and namespace-uri()='" + CORE_NS_URI + "']";

  public CollectionSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  protected String getMatchingAggregatorName() {
    return COLLECTION_AGGREGATOR;
  }
}
