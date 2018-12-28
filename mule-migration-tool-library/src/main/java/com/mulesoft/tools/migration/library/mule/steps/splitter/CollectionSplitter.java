package com.mulesoft.tools.migration.library.mule.steps.splitter;

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
  protected String getSplitterName() {
    return "collection-splitter";
  }
}
