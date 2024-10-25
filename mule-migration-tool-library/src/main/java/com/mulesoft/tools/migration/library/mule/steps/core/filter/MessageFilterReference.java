/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate global message filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessageFilterReference extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + CORE_NS_URI + "' and local-name()='processor' and @ref]";

  @Override
  public String getDescription() {
    return "Update global message-filters.";
  }

  public MessageFilterReference() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    getApplicationModel().getNodeOptional("/*/mule:message-filter[@name = '" + element.getAttributeValue("ref") + "']")
        .ifPresent(globalFilter -> {
          globalFilter.setAttribute("globalProcessed", "true", MIGRATION_NAMESPACE);
          Element clonedFilter = globalFilter.clone();
          clonedFilter.removeAttribute("name");
          addElementAfter(clonedFilter, element);
          element.detach();
        });
  }
}
