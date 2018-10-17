/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.ArrayList;

/**
 * Migration of the All component on validation module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationAllProcessorMigration extends AbstractApplicationModelMigrationStep {

  private static final String VALIDATION_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/validation";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + VALIDATION_NAMESPACE_URI + "'"
      + " and local-name()='validations']";

  @Override
  public String getDescription() {
    return "Migrate All component on Validation Module.";
  }

  public ValidationAllProcessorMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Element parentElement = element.getParentElement();
    for (Element child : new ArrayList<>(element.getChildren())) {
      child.detach();
      parentElement.addContent(child);
    }
    element.detach();
  }


}
