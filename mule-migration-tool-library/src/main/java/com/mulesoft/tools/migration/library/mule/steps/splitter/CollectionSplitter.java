/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
