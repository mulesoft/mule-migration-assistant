/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Migrate not-filter to validations
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class NotFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='not-filter']";

  @Override
  public String getDescription() {
    return "Update not-filter to validations.";
  }

  public NotFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (element.getChildren().isEmpty()) {
      element.detach();
    } else {
      addValidationsModule(element.getDocument());

      element.setNamespace(VALIDATION_NAMESPACE);

      boolean concatenableChildren = true;
      Collection<String> childrenExpressions = new ArrayList<>();
      for (Element childFilter : element.getChildren()) {
        if ("is-true".equals(childFilter.getName()) && VALIDATION_NAMESPACE.equals(childFilter.getNamespace())) {
          childrenExpressions.add(childFilter.getAttributeValue("expression"));
        } else {
          concatenableChildren = false;
          break;
        }
      }

      if (concatenableChildren && childrenExpressions.size() == 1) {
        report.report(WARN, element, element,
                      "Filters are replaced with the validations module",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-validation");

        String uberExpr = childrenExpressions.stream().findFirst().get();
        new ArrayList<>(element.getChildren()).forEach(c -> c.detach());

        element.setAttribute("expression", uberExpr);
        element.setName("is-false");
        element.setNamespace(VALIDATION_NAMESPACE);
      } else {
        report.report(ERROR, element, element,
                      "Replace 'not-filter with a single expression on a validator'",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-validation");
      }

      handleFilter(element);
    }
  }
}
