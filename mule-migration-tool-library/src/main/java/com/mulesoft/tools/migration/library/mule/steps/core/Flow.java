/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate flow definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Flow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/*/mule:flow";

  @Override
  public String getDescription() {
    return "Migrate flow definitions";
  }

  public Flow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setAttribute("name", element.getAttributeValue("name")
        .replaceAll("/", "\\\\")
        .replaceAll("\\[|\\{", "(")
        .replaceAll("\\]|\\}", ")")
        .replaceAll("#", "_"));

    if (element.getAttribute("processingStrategy") != null) {
      if ("synchronous".equals(element.getAttributeValue("processingStrategy"))) {
        element.setAttribute("maxConcurrency", "1");
      }

      element.removeAttribute("processingStrategy");
      report.report(WARN, element, element, "'flow' no longer has a 'processingStrategy' attribute.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-engine");
    }
  }


}
