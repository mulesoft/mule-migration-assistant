/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
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
        if ("flow".equals(flow.getName())) {
          Element errorHandler = getFlowExceptionHandlingElement(flow);

          if (errorHandler == null) {
            errorHandler = new Element("error-handler", CORE_NAMESPACE);
            flow.addContent(errorHandler);
          }

          resolveValidationHandler(errorHandler);
        } else {
          Element wrappingTry = new Element("try", CORE_NAMESPACE);

          addElementAfter(wrappingTry, filter);
          wrappingTry.addContent(filter.clone());
          filter.detach();

          Element errorHandler = new Element("error-handler", CORE_NAMESPACE);
          wrappingTry.addContent(errorHandler);
          resolveValidationHandler(errorHandler);
        }
      }
    }
  }

  protected Element resolveValidationHandler(Element errorHandler) {
    return errorHandler.getChildren().stream()
        .filter(c -> "on-error-propagate".equals(c.getName()) && "MULE:VALIDATION".equals(c.getAttributeValue("type")))
        .findFirst().orElseGet(() -> {
          Element validationHandler = new Element("on-error-propagate", CORE_NAMESPACE)
              .setAttribute("type", "MULE:VALIDATION")
              .setAttribute("logException", "false");
          errorHandler.addContent(0, validationHandler);
          validationHandler.addContent(new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", "filtered")
              .setAttribute("value", "#[true]"));
          return validationHandler;
        });
  }
}
