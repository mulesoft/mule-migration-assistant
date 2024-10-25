/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.Optional;

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
      Optional<Element> processingStrategyConfig =
          getApplicationModel().getNodeOptional("//*[@name = '" + processingStrategy.getValue() + "']");
      processingStrategyConfig.ifPresent(Element::detach);
      report.report("async.processingStrategy", element, element);
    }
  }
}
