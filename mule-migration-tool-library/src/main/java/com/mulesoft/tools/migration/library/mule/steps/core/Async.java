/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate Async
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Async extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:*[local-name()='async']";

  @Override
  public String getDescription() {
    return "Migrate Async.";
  }

  public Async() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    //    TODO Migrate to maxConcurrency when mule version is equal or higher to 4.2.0
    Attribute processingStrategy = element.getAttribute("processingStrategy");
    if (processingStrategy != null) {
      element.removeAttribute(processingStrategy);
      Element processingStrategyConfig = getApplicationModel().getNode("//*[@name = '" + processingStrategy.getValue() + "']");
      if (processingStrategyConfig != null) {
        processingStrategyConfig.detach();
      }
      report.report("async.processingStrategy", element, element);
    }
  }
}
