/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
