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
 * Migrate Regex Filter to the a validation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RegexFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='regex-filter']";

  @Override
  public String getDescription() {
    return "Update Regex filter to a validation.";
  }

  public RegexFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    element.getAttribute("pattern").setName("regex");
    element.setName("matches-regex");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
