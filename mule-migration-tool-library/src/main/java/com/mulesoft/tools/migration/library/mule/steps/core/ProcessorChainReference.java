/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

/**
 * Migration step for Processor component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ProcessorChainReference extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("processor");

  public ProcessorChainReference() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Update Processor component.";
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("flow-ref");
    object.setAttribute("name", object.getAttributeValue("ref"));
    object.removeAttribute("ref");
  }
}
