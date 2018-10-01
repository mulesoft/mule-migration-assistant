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
 * Migrate or-filter to validations
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OrFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='or-filter']";

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

    // if (element.getChildren().isEmpty()) {
    // element.detach();
    // } else {
    // addValidationsModule(element.getDocument());
    //
    // element.setNamespace(VALIDATION_NAMESPACE);
    //
    // boolean concatenableChildren = true;
    // Collection<String> childrenExpressions = new ArrayList<>();
    // for (Element childFilter : element.getChildren()) {
    // if ("is-true".equals(childFilter.getName()) && VALIDATION_NAMESPACE.equals(childFilter.getNamespace())) {
    // childrenExpressions.add(childFilter.getAttributeValue("expression"));
    // } else {
    // concatenableChildren = false;
    // break;
    // }
    // }
    //
    // if (concatenableChildren) {
    // report.report(WARN, element, element,
    // "Filters are replaced with the validations module",
    // "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-validation");
    //
    // String uberExpr =
    // childrenExpressions.stream().map(expr -> getExpressionMigrator().unwrap(expr)).collect(joining(" || ", "#[", "]"));
    // new ArrayList<>(element.getChildren()).forEach(c -> c.detach());
    //
    // element.setAttribute("expression", uberExpr);
    // element.setName("is-true");
    // element.setNamespace(VALIDATION_NAMESPACE);
    // } else {
    // report.report(ERROR, element, element,
    // "Replace 'or-filter with a single expression on a validator'",
    // "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-validation");
    // }
    //
    // handleFilter(element);
    // }
  }
}
