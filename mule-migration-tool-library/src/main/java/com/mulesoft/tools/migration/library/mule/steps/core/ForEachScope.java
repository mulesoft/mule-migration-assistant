/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

/**
 * Migration of For Each Scope
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ForEachScope extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='foreach']";
  private static final String JSON_TRANSFORMER_NAME = "json-to-object-transformer";
  private static final String BYTE_ARRAY_TRANSFORMER_NAME = "byte-array-to-object-transformer";

  @Override
  public String getDescription() {
    return "Update For Each Scope.";
  }

  public ForEachScope() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element) throws RuntimeException {
    try {
      Element transformerToRemove = getTransformerToRemove(element);
      if (transformerToRemove != null) {
        transformerToRemove.detach();
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Failed to migrate references to Exception Strategies.");
    }
  }

  private Element getTransformerToRemove(Element element) {
    Integer elementIndex = element.getParentElement().getChildren().indexOf(element);
    if (elementIndex > 0) {
      Element previousElement = element.getParentElement().getChildren().get(elementIndex - 1);
      if (StringUtils.equals(previousElement.getName(), JSON_TRANSFORMER_NAME)
          || StringUtils.equals(previousElement.getName(), BYTE_ARRAY_TRANSFORMER_NAME)) {
        return previousElement;
      }
    } else {
      return null;
    }
    return null;
  }
}
