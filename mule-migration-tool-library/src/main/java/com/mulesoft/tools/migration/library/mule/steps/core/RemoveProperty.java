/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Remove Property to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveProperty extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[local-name()='remove-property']";

  @Override
  public String getDescription() {
    return "Update Remove Property namespace to compatibility.";
  }

  public RemoveProperty() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report(WARN, element, element,
                  "Instead of using properties in the flow, just don't use them in the listener/operation.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
    element.setNamespace(COMPATIBILITY_NAMESPACE);
  }
}
