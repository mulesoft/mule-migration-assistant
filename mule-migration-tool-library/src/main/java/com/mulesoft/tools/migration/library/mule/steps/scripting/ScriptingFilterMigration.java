/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
