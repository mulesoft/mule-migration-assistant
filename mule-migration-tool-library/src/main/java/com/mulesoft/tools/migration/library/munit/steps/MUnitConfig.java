/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
