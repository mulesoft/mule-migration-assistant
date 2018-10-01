/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementsAfter;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

/**
 * Migrate global message filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessageFilterReference extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='processor' and @ref]";

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
          globalFilter.setAttribute("globalProcessed", "true", Namespace.getNamespace("migration", "migration"));
          Element clonedFilter = globalFilter.clone();
          clonedFilter.removeAttribute("name");
          addElementAfter(clonedFilter, element);
          element.detach();
        });
  }
}
