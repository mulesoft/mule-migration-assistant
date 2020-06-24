/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate global filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FilterReference extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + CORE_NS_URI + "' and local-name()='filter' and @ref]";

  @Override
  public String getDescription() {
    return "Update global filters.";
  }

  public FilterReference() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Element globalFilter = getApplicationModel().getNode("/*/*[@name = '" + element.getAttributeValue("ref") + "']");
    globalFilter.setAttribute("globalProcessed", "true", Namespace.getNamespace("migration", "migration"));
    Element clonedFilter = globalFilter.clone();
    clonedFilter.removeAttribute("name");
    addElementAfter(clonedFilter, element);
    element.detach();

  }
}
