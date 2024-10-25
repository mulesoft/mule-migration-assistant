/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migration of I18-N Configuration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationI18NMigration extends AbstractApplicationModelMigrationStep {

  private static final String VALIDATION_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/validation";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + VALIDATION_NAMESPACE_URI + "'"
      + " and local-name()='i18-n-config']";

  @Override
  public String getDescription() {
    return "Migrate I18-N Global Config";
  }

  public ValidationI18NMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Element parentElement = element.getParentElement();
    if (parentElement.getName().equals("config")) {
      element.setName("i18-n");
    } else {
      element.setName("config");
      Element childNode = new Element("i18n", element.getNamespace());
      copyAttributeIfPresent(element, childNode, "bundlePath");
      copyAttributeIfPresent(element, childNode, "locale");
      element.addContent(childNode);
    }
  }
}
