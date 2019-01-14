/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import org.jdom2.Element;

public class CollectionSplitter extends AbstractSplitter {

  private static final String XPATH_SELECTOR = "//*[local-name()='collection-splitter']";

  public CollectionSplitter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  protected String getMatchingAggregatorName() {
    return "collection-aggregator";
  }

  @Override
  protected void setForEachExpressionAttribute(Element splitterElement, Element forEachElement) {
    //Do nothing, use default attribute
  }
}
