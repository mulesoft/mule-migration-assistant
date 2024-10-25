/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Wildcard Filter to the a validation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class WildcardFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("wildcard-filter");

  @Override
  public String getDescription() {
    return "Update Wildcard filter to a validation.";
  }

  public WildcardFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    element.setAttribute("regex", "^" + element.getAttributeValue("pattern").replaceAll("\\*", ".*") + "$");
    element.removeAttribute("pattern");
    element.setAttribute("value", "#[payload]");
    element.setName("matches-regex");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
