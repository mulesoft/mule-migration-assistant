/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlowExceptionHandlingElement;

import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration;

import org.jdom2.Element;

/**
 * Generic filter migration support
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AbstractFilterMigrator extends ValidationMigration {

  protected void handleFilter(Element filter) {
    if (!(filter.getParentElement().getNamespace().equals(VALIDATION_NAMESPACE)
        && filter.getParentElement().getName().endsWith("filter"))) {
      Element flow = getFlow(filter);

      if (flow != null) {
        Element errorHandler = getFlowExceptionHandlingElement(flow);

        if (errorHandler == null) {
          errorHandler = new Element("error-handler", CORE_NAMESPACE);
          flow.addContent(errorHandler);
        }

        resolveValidationHandler(errorHandler);
      }
    }
  }

  protected Element resolveValidationHandler(Element errorHandler) {
    return errorHandler.getChildren().stream()
        .filter(c -> "on-error-continue".equals(c.getName()) && "MULE:VALIDATION".equals(c.getAttributeValue("type")))
        .findFirst().orElseGet(() -> {
          Element validationHandler = new Element("on-error-continue", CORE_NAMESPACE).setAttribute("type", "MULE:VALIDATION");
          errorHandler.addContent(validationHandler);
          validationHandler.addContent(new Element("logger", CORE_NAMESPACE)
              .setAttribute("level", "DEBUG")
              .setAttribute("message", "Message filtered"));
          return validationHandler;
        });
  }
}
