/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.component;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate null-component to a raise-error
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class NullComponent extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='null-component']";

  @Override
  public String getDescription() {
    return "Migrate null-component to a raise-error.";
  }

  public NullComponent() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("raise-error");
    if (!object.getChildren().isEmpty()) {
      report.report(ERROR, object, object, "Interceptors have been replaced by custom policies in Mule 4.",
                    "https://docs.mulesoft.com/api-manager/2.x/custom-policy-index-latest");
      object.removeContent();
    }
    object.setAttribute("type", "COMPATIBILITY:UNSUPPORTED");
    object.setAttribute("description", "This service cannot receive messages");
  }

}
