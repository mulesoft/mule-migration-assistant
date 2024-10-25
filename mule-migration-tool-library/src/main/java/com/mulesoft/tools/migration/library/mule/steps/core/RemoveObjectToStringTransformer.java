/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
