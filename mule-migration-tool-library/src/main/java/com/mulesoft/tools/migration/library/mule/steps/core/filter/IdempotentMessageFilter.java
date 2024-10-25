/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Idempotent Message Filter to Idempotent-Message Validator
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class IdempotentMessageFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("idempotent-message-filter");

  @Override
  public String getDescription() {
    return "Update Idempotent Message Filter to Idempotent-Message Validator.";
  }

  public IdempotentMessageFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setName("idempotent-message-validator");
    handleFilter(element);
  }

  @Override
  protected Element resolveValidationHandler(Element errorHandler) {
    return errorHandler.getChildren().stream()
        .filter(c -> "on-error-propagate".equals(c.getName()) && "DUPLICATE_MESSAGE".equals(c.getAttributeValue("type")))
        .findFirst().orElseGet(() -> {
          Element validationHandler = new Element("on-error-propagate", CORE_NAMESPACE)
              .setAttribute("type", "DUPLICATE_MESSAGE")
              .setAttribute("logException", "false");
          errorHandler.addContent(0, validationHandler);
          validationHandler.addContent(new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", "filtered")
              .setAttribute("value", "#[true]"));
          return validationHandler;
        });
  }

}
