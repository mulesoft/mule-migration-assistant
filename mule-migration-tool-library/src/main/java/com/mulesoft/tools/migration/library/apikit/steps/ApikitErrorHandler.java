/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

import static com.mulesoft.tools.migration.project.model.ApplicationModelUtils.changeNodeName;
import static java.util.stream.Collectors.toList;

/**
 * Migrates the router configuration of APIkit
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApikitErrorHandler extends AbstractApikitMigrationStep {

  private static final String XPATH_SELECTOR =
      "//*[local-name()='mapping-exception-strategy' and namespace-uri()='" + APIKIT_NAMESPACE_URI + "']";

  @Override
  public String getDescription() {
    return "Update APIkit Mapping Exception Strategy";
  }

  public ApikitErrorHandler() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    final List<Element> mappings = element.getChildren("mapping", element.getNamespace());

    final List<Element> errorMappings = mappings.stream()
        .map(this::buildOnErrorFromApikitMapping)
        .collect(toList());

    element.removeChildren("mapping", element.getNamespace());
    element.addContent(errorMappings);
    changeNodeName("", "error-handler").apply(element);

    migrateWhenExpression(element);
  }

  private Element buildOnErrorFromApikitMapping(Element mapping) {
    final Namespace rootNamespace = mapping.getDocument().getRootElement().getNamespace();
    final Element newOnError = new Element("on-error-propagate", rootNamespace);

    // Setting type
    final Element exception = mapping.getChild("exception", mapping.getNamespace());
    final Attribute exceptionAttribute = exception.getAttribute("value");

    if (exceptionAttribute != null) {
      final String exceptionClass = exceptionAttribute.getValue();

      final String errorType = getErrorForException(exceptionClass);

      if (errorType != null)
        newOnError.setAttribute("type", errorType);
      else
        newOnError.setAttribute("when", "#[mel:exception.causedBy(" + exceptionClass + ")]");
    }

    // Migrating mapping status code to var
    final String value = mapping.getAttributeValue("statusCode");
    final Element httpStatusVariable = buildSetVariable("httpStatus", value, rootNamespace);
    newOnError.addContent(httpStatusVariable);

    // Copy children to new node
    mapping.getChildren().stream()
        .filter(e -> !e.getNamespace().equals(mapping.getNamespace()))
        .forEach(e -> newOnError.addContent(e.clone()));

    return newOnError;
  }

  private Element buildSetVariable(String name, String value, Namespace rootNamespace) {
    final Element httpStatusVariable = new Element("set-variable", rootNamespace);
    httpStatusVariable.setAttribute("variableName", name);
    httpStatusVariable.setAttribute("value", value);
    return httpStatusVariable;
  }


  private void migrateWhenExpression(Element element) {
    if (element.getAttribute("when") != null) {
      Attribute whenCondition = element.getAttribute("when");
      whenCondition.setValue(getExpressionMigrator().migrateExpression(whenCondition.getValue(), true, element));
    }
  }

  private String getErrorForException(String exceptionClass) {
    switch (exceptionClass) {
      case "org.mule.module.apikit.exception.BadRequestException":
        return "APIKIT:BAD_REQUEST";
      case "org.mule.module.apikit.exception.NotFoundException":
        return "APIKIT:NOT_FOUND";
      case "org.mule.module.apikit.exception.MethodNotAllowedException":
        return "APIKIT:METHOD_NOT_ALLOWED";
      case "org.mule.module.apikit.exception.NotAcceptableException":
        return "APIKIT:NOT_ACCEPTABLE";
      case "org.mule.module.apikit.exception.UnsupportedMediaTypeException":
        return "APIKIT:UNSUPPORTED_MEDIA_TYPE";
      default:
        return null;
    }
  }
}
