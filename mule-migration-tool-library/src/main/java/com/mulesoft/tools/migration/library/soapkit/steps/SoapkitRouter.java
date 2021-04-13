/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.steps;

import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.addElement;
import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.replaceSlashesByBackSlashes;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the router of APIkit for SOAP
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SoapkitRouter extends AbstractSoapkitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='router' and namespace-uri()='" + SOAPKIT_NAMESPACE_URI + "']";

  @Override
  public String getDescription() {
    return "Update APIkit for SOAP router";
  }

  public SoapkitRouter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) {
    replaceSlashesByBackSlashes(element, "config-ref");

    addElement(element, "message", "#[payload]");
    addElement(element, "attributes", getAttributesMapping());
  }

  private String getAttributesMapping() {
    return "#[%dw 2.0\n" +
        "output application/java\n" +
        "---\n" +
        "{\n" +
        "   headers: attributes.headers,\n" +
        "   method: attributes.method,\n" +
        "   queryString: attributes.queryString\n" +
        "}]";
  }

}
