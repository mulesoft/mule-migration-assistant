/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes.MIGRATION_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Utils class for useful splitter and aggregator related methods.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SplitterAggregatorUtils {

  private static final String AGGREGATOR_PROCESSED_ATTRIBUTE = "processed";

  public static void setAggregatorAsProcessed(Element aggregatorElement) {
    addMigrationAttributeToElement(aggregatorElement, new Attribute(AGGREGATOR_PROCESSED_ATTRIBUTE, "true"));
  }

  public static boolean isAggregatorProcessed(Element aggregatorElement) {
    String processedAttributeValue = aggregatorElement.getAttributeValue(AGGREGATOR_PROCESSED_ATTRIBUTE, MIGRATION_NAMESPACE);
    return processedAttributeValue != null && Boolean.valueOf(processedAttributeValue);
  }


}
