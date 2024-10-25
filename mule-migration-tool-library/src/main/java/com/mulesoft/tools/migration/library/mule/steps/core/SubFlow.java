/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.core.Flow.migrateFlowName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getTopLevelCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate sub-flow definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SubFlow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getTopLevelCoreXPathSelector("sub-flow");

  @Override
  public String getDescription() {
    return "Migrate sub-flow definitions";
  }

  public SubFlow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    String name = element.getAttributeValue("name");

    // do not replace special chars for sub-flows in policy templates
    if (!name.matches(".*\\{\\{\\w+}}.*")) {
      element.setAttribute("name", migrateFlowName(name));
    }

  }

}
