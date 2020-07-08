/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.steps;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.library.soapkit.helpers.DocumentHelper.replaceSlashesByBackSlashes;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

/**
 * Migrates the router configuration of APIkit for SOAP
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SoapkitFault extends AbstractSoapkitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='fault' and namespace-uri()='" + SOAPKIT_NAMESPACE_URI + "']";

  @Override
  public String getDescription() {
    return "Update APIkit for SOAP fault";
  }

  public SoapkitFault() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) {
    replaceSlashesByBackSlashes(element, "config-ref");
    migrateFaultType(element, report);
  }

  private void migrateFaultType(Element element, MigrationReport report) {
    final Attribute faultType = element.getAttribute("faultType");

    if (faultType != null) {
      final String faultTypeValue = faultType.getValue();
      final String[] split = faultTypeValue.split("\\|\\|");

      if (split.length <= 0)
        throw new RuntimeException("Error parsing 'faultType' value");

      final String faultValue;
      final String operationValue;

      if (split.length == 1) {
        report.report(WARN, element, element.getParentElement(), "Cannot find value for 'operation'");
        operationValue = "";
        faultValue = split[0];
      } else {
        if (split.length > 2)
          report
              .report(WARN, element, element.getParentElement(),
                      "fault type shouldn't have more than two partes '<Operation>||<FaultType>'. Some data could be lost after migration");
        operationValue = split[0];
        faultValue = split[1];
      }

      element.setAttribute("operation", operationValue);
      element.setAttribute("fault", faultValue);
      element.removeAttribute(faultType);
    }
  }
}
