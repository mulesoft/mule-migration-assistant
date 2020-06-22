/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove Object to String Transformer.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveObjectToStringTransformer extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("object-to-string-transformer");

  @Override
  public String getDescription() {
    return "Remove Object to String Transformer.";
  }

  public RemoveObjectToStringTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element != null && element.getParentElement() == element.getDocument().getRootElement()) {
      getApplicationModel().getNodes("//mule:transformer[@ref='" + element.getAttributeValue("name") + "']")
          .forEach(t -> t.detach());
    }
    try {
      element.detach();
    } catch (Exception ex) {
      throw new MigrationStepException("Failed to remove Object to String Transformer.");
    }
  }
}
