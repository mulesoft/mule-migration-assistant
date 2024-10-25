/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlowExceptionHandlingElement;

import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Generic filter migration support
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AbstractFilterMigrator extends ValidationMigration {

  private static final String DOCS_NAMESPACE_URL = "http://www.mulesoft.org/schema/mule/documentation";
  private static final String DOCS_NAMESPACE_PREFIX = "doc";
  private static final Namespace DOCS_NAMESPACE = Namespace.getNamespace(DOCS_NAMESPACE_PREFIX, DOCS_NAMESPACE_URL);

  protected void handleFilter(Element filter) {
    if (filter.getAttribute("name") != null) {
      Attribute nameAttribute = filter.getAttribute("name");
      if (filter.getAttribute("name", DOCS_NAMESPACE) != null) {
        nameAttribute.detach();
      } else {
        filter.getDocument().getRootElement().addNamespaceDeclaration(DOCS_NAMESPACE);
        nameAttribute.setNamespace(DOCS_NAMESPACE);
      }
    }
    if (!(filter.getParentElement().getNamespace().equals(VALIDATION_NAMESPACE)
        && filter.getParentElement().getName().endsWith("filter"))) {
      Element flow = getContainerElement(filter);

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
