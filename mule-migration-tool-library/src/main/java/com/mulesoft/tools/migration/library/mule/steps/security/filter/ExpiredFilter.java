/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.filter;

import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution.addValidationDependency;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Update filter-expired filter to use the validations module.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExpiredFilter extends AbstractFilterMigrator {

  private static final String FILTERS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/filters";
  private static final Namespace FILTERS_NAMESPACE = getNamespace("filters", FILTERS_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + FILTERS_NAMESPACE_URI + "' and local-name() = 'filter-expired']";

  @Override
  public String getDescription() {
    return "Update filter-expired filter to use the validations module.";
  }

  public ExpiredFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(FILTERS_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationNamespace(element.getDocument());
    addValidationDependency(getApplicationModel().getPomModel().get());

    element.setName("is-not-elapsed");
    element.setNamespace(VALIDATION_NAMESPACE);

    migrateExpression(element.getAttribute("dateTime"), getExpressionMigrator());
    element.getAttribute("dateTime").setName("since");

    final Attribute expiresInAttr = element.getAttribute("expiresIn");
    if (expiresInAttr != null) {
      expiresInAttr.setValue(expiresInAttr.getValue());
      expiresInAttr.setName("time");
    } else {
      element.setAttribute("time", "30000");
    }

    element.setAttribute("timeUnit", "MILLISECONDS");

    handleFilter(element);
  }

}
