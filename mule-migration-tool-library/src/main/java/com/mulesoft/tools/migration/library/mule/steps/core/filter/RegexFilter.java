/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate Regex Filter to the a validation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RegexFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("regex-filter");

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

    final Attribute attrPattern = element.getAttribute("pattern");

    // Mule 3 filter does something like a 'contains' of the pattern, not an actual regex.
    if (!attrPattern.getValue().endsWith(".*")) {
      attrPattern.setValue(attrPattern.getValue() + ".*");
    }
    if (!attrPattern.getValue().startsWith(".*")) {
      attrPattern.setValue(".*" + attrPattern.getValue());
    }

    attrPattern.setName("regex");

    if (element.getAttribute("value") == null) {
      element.setAttribute("value", "#[payload]");
    }

    element.setName("matches-regex");
    element.setNamespace(VALIDATION_NAMESPACE);

    handleFilter(element);
  }

}
