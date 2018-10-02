/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Custom Filter to the a validation stub
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CustomFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='custom-filter']";

  @Override
  public String getDescription() {
    return "Update Custom Filter to a validation stub.";
  }

  public CustomFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report(ERROR, element, element,
                  "Filters are replaced with the validations module",
                  "https://docs.mulesoft.com/mule-runtime/4.1/migration-filters#migrating_custom_or_complex_filters");

    addValidationsModule(element.getDocument());

    element.setAttribute("expression",
                         "#[true /* replicate the logic of '" + element.getAttributeValue("class") + "' in DataWeave */]");
    element.removeAttribute("class");
    element.setName("is-true");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
