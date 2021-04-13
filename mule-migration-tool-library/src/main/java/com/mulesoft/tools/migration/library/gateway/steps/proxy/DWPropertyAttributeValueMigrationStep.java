/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate DW properties values
 *
 * @author Mulesoft Inc.
 */
public class DWPropertyAttributeValueMigrationStep extends GatewayMigrationStep {

  private static final String PROPERTY_ATTRIBUTE_VALUE_START = "![p[";
  private static final String PROPERTY_ATTRIBUTE_VALUE_END = "']]";
  private static final String NEW_PROPERTY_ATTRIBUTE_VALUE_START = "${";
  private static final String NEW_PROPERTY_ATTRIBUTE_VALUE_END = "}";

  public DWPropertyAttributeValueMigrationStep() {
    this.setAppliedTo(getXPathSelector(PROPERTY_ATTRIBUTE_VALUE_START));
  }

  private void replaceValue(Attribute attribute) {
    attribute.setValue(attribute.getValue().replace(PROPERTY_ATTRIBUTE_VALUE_START + "'", NEW_PROPERTY_ATTRIBUTE_VALUE_START)
        .replace(PROPERTY_ATTRIBUTE_VALUE_END, NEW_PROPERTY_ATTRIBUTE_VALUE_END));
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.getAttributes().stream().filter(attr -> attr.getValue().startsWith(PROPERTY_ATTRIBUTE_VALUE_START))
        .forEach(attribute -> replaceValue(attribute));
  }
}
