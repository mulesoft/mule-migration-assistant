/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import org.jdom2.Element;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Migrate references of exception strategies
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SmartgateExceptionStrategyRef extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("exception-strategy");

  @Override
  public String getDescription() {
    return "Update references to Exception Strategies.";
  }

  public SmartgateExceptionStrategyRef() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    changeNodeName("", "error-handler")
        .apply(element);
    element.getAttribute("ref").setValue("global-error-handler");
    element.removeContent();

    if (element.getParentElement().getName().equals("error-handler")) {
      report.report("errorHandling.reuse", element, element);
    }
  }
}
