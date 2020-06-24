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
 * Migrate PayloadType Filter to the a validation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PayloadTypeFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("payload-type-filter");

  @Override
  public String getDescription() {
    return "Update PayloadType to a validation.";
  }

  public PayloadTypeFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    element.setAttribute("expression",
                         "#[payload.^class == '" + element.getAttributeValue("expectedType") + "']");
    element.removeAttribute("expectedType");
    element.setName("is-true");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
