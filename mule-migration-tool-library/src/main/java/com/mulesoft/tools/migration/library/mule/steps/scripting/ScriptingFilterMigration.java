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
package com.mulesoft.tools.migration.library.mule.steps.scripting;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.VALIDATION_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlowExceptionHandlingElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Update scripting filter.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ScriptingFilterMigration extends ScriptingModuleMigration {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + SCRIPT_NAMESPACE_URI + "' and local-name()='filter']";

  public ScriptingFilterMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SCRIPT_NAMESPACE));
  }

  @Override
  public String getDescription() {
    return "Update scripting filter.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    super.execute(element, report);
    element.setAttribute("target", "filterAccepted");
    addElementAfter(new Element("is-true", VALIDATION_NAMESPACE).setAttribute("expression", "#[vars.filterAccepted]"), element);
    handleFilter(element);
  }

  @Override
  protected void handleCode(Element scriptNode) {
    setText(scriptNode, scriptNode.getChild("text", SCRIPT_NAMESPACE).getText());
  }

  protected void handleFilter(Element filter) {
    if (!(filter.getParentElement().getNamespace().equals(VALIDATION_NAMESPACE)
        && filter.getParentElement().getName().endsWith("filter"))) {
      Element flow = getContainerElement(filter);

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
