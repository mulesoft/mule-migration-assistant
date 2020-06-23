/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getXPathSelector;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Element;

/**
 * This steps migrates the MUnit 1.x config
 * @author Mulesoft Inc.
 */
public class MUnitConfig extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR = getXPathSelector("http://www.mulesoft.org/schema/mule/munit", "config", true);
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_MOCK_CONNECTORS = "mock-connectors";
  private static final String ATTRIBUTE_MOCK_INBOUNDS = "mock-inbounds";

  @Override
  public String getDescription() {
    return "Update MUnit config";
  }

  public MUnitConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      File munitFile = new File(element.getDocument().getBaseURI());
      changeAttribute(ATTRIBUTE_NAME, empty(), of(FilenameUtils.getBaseName(munitFile.getName()))).apply(element);
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }

    if (element.getAttribute(ATTRIBUTE_MOCK_CONNECTORS) != null) {
      if (element.getAttributeValue(ATTRIBUTE_MOCK_CONNECTORS).equals("true")) {
        report.report("munit.mockConnectors", element, element.getParentElement());
      }
      element.removeAttribute(ATTRIBUTE_MOCK_CONNECTORS);
    }

    if (element.getAttribute(ATTRIBUTE_MOCK_INBOUNDS) != null) {
      if (element.getAttributeValue(ATTRIBUTE_MOCK_INBOUNDS).equals("true")) {
        report.report("munit.mockInbounds", element, element.getParentElement());
      }
      element.removeAttribute(ATTRIBUTE_MOCK_INBOUNDS);
    }

  }
}
