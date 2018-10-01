/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate ExceptionType Filter to the a validation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExceptionTypeFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='exception-type-filter']";

  @Override
  public String getDescription() {
    return "Update ExceptionType to a validation.";
  }

  public ExceptionTypeFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    element.setAttribute("expression",
                         "#[error.cause.^class == '" + element.getAttributeValue("expectedType") + "']");
    element.removeAttribute("expectedType");
    element.setName("is-true");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
