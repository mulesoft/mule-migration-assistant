/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate Expression Filter to the a validation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExpressionFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("expression-filter");

  @Override
  public String getDescription() {
    return "Update Expression filter to a validation.";
  }

  public ExpressionFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    addValidationsModule(element.getDocument());

    element.setName("is-true");
    element.setNamespace(VALIDATION_NAMESPACE);

    final Attribute expression = element.getAttribute("expression");
    expression.setValue(getExpressionMigrator()
        .migrateExpression(getExpressionMigrator().isWrapped(expression.getValue()) ? expression.getValue()
            : getExpressionMigrator().wrap(expression.getValue()), true, element));

    handleFilter(element);
  }

}
