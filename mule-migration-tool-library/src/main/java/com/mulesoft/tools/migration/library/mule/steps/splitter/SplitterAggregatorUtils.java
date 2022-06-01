/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements.MIGRATION_NAMESPACE;
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
