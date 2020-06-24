/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Custom Filter to the a validation stub
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CustomFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("custom-filter");

  @Override
  public String getDescription() {
    return "Update Custom Filter to a validation stub.";
  }

  public CustomFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report("filters.replacedWithValidations", element, element);

    addValidationsModule(element.getDocument());

    element.setAttribute("expression",
                         "#[true /* replicate the logic of '" + element.getAttributeValue("class") + "' in DataWeave */]");
    element.removeAttribute("class");
    element.setName("is-true");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
