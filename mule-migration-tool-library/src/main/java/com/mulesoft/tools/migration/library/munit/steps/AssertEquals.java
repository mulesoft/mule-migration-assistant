/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeAttribute;
import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.function.Function;

/**
 * This steps migrates the MUnit 1.x assert-payload-equals
 *
 * @author Mulesoft Inc.
 */
public class AssertEquals extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='assert-on-equals']";

  @Override
  public String getDescription() {
    return "Update Assert Equals to new MUnit Assertion component";
  }

  public AssertEquals() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    try {
      changeNodeName("munit-tools", "assert-that")
          .andThen(changeAttribute("expectedValue", of("expression"), empty()))
          .andThen(changeAttribute("actualValue", of("is"), empty()))
          .apply(element);

      updateMUnitAssertionEqualsExpression("is")
          .apply(element);

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }


  protected Function<Element, Element> updateMUnitAssertionEqualsExpression(String attributeName) {
    return e -> {
      Attribute attribute = e.getAttribute(attributeName);
      if (attribute != null) {
        String attributeValue = attribute.getValue().trim();
        if (attributeValue.startsWith("#[")) {
          StringBuffer sb = new StringBuffer(attributeValue);
          sb.replace(0, sb.indexOf("[") + 1, "#[MUnitTools::equalTo(");
          sb.replace(sb.lastIndexOf("]"), sb.lastIndexOf("]") + 1, ")]");
          attributeValue = sb.toString();
        } else {
          attributeValue = "#[MUnitTools::equalTo(" + attributeValue + ")]";
        }
        attribute.setValue(attributeValue);
      }
      return e;
    };
  }

}
