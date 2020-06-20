/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
