/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
