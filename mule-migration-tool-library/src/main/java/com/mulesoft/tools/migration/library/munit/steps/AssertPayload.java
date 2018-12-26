/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.addAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getXPathSelector;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * This steps migrates the MUnit 1.x assert-payload
 * @author Mulesoft Inc.
 */
public class AssertPayload extends AbstractAssertionMigration {

  private static final String XPATH_SELECTOR =
      getXPathSelector("http://www.mulesoft.org/schema/mule/munit", "assert-payload-equals");

  @Override
  public String getDescription() {
    return "Update Assert Payload-Equals to new MUnit Assertion component";
  }

  public AssertPayload() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName("munit-tools", "assert-that")
          .andThen(addAttribute("expression", "#[payload]"))
          .andThen(changeAttribute("expectedValue", of("is"), empty()))
          .apply(element);

      Attribute isAttribute = element.getAttribute("is");
      if (isAttribute != null) {
        String attributeValue = isAttribute.getValue();
        if (getExpressionMigrator().isWrapped(attributeValue)) {
          attributeValue = "#[MunitTools::equalTo(" + getExpressionMigrator().unwrap(attributeValue) + ")]";
        } else {
          attributeValue = "#[MunitTools::equalTo(" + attributeValue + ")]";
        }
        isAttribute.setValue(attributeValue);
      }
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }
}
