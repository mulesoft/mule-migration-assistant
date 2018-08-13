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

/**
 * Migrate sub-flow definitions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SubFlow extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/*/mule:sub-flow";

  @Override
  public String getDescription() {
    return "Migrate sub-flow definitions";
  }

  public SubFlow() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setAttribute("name", element.getAttributeValue("name")
        .replaceAll("\\/", "\\\\")
        .replaceAll("\\[|\\{", "(")
        .replaceAll("\\]|\\}", ")")
        .replaceAll("#", "_"));
  }


}
