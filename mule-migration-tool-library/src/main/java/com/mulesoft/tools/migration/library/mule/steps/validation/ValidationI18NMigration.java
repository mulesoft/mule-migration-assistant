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

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

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
