/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate references of exception strategies
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExceptionStrategyRef extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("exception-strategy");

  @Override
  public String getDescription() {
    return "Update references to Exception Strategies.";
  }

  public ExceptionStrategyRef() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("", "error-handler")
        .apply(element);

    if (element.getParentElement().getName().equals("error-handler")) {
      report.report("errorHandling.reuse", element, element);
    }
  }
}
