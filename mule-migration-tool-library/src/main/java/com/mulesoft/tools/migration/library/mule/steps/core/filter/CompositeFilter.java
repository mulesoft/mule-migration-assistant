/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate Filters to the compatibility plugin
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CompositeFilter extends AbstractApplicationModelMigrationStep {

  private static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";

  public static final String XPATH_SELECTOR =
      "//*[local-name()='not-filter' or local-name()='and-filter' or local-name()='or-filter']";

  @Override
  public String getDescription() {
    return "Update Filters namespace to compatibility.";
  }

  public CompositeFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getChildren().isEmpty()) {
      element.detach();
    } else {
      report.report(WARN, element, element,
                    "Filters are replaced with the validations module",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-validation");
      element.setNamespace(Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE));
    }
  }
}
