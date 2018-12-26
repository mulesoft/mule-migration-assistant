/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate or-filter to validations
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OrFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("or-filter");

  @Override
  public String getDescription() {
    return "Update or-filter to validations.";
  }

  public OrFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getChildren().isEmpty()) {
      element.detach();
    } else {
      addValidationsModule(element.getDocument());

      element.setName("any");
      element.setNamespace(VALIDATION_NAMESPACE);

      handleFilter(element);
    }
  }
}
