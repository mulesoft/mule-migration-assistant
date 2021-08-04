/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.steps;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isTopLevelElement;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates http mappings made by APIkit
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApikitHttpListenerMapping extends AbstractApikitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='router' and namespace-uri()='" + APIKIT_NAMESPACE_URI + "']";
  private static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";
  private static final Namespace HTTP_NAMESPACE = Namespace.getNamespace(HTTP_NAMESPACE_URI);
  private static final String STATUS_CODE_ATTR_NAME = "statusCode";

  @Override
  public String getDescription() {
    return "Update APIkit Http Listener Mappings";
  }

  public ApikitHttpListenerMapping() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    final Element flow = getParentFlow(element);

    if (flow != null) {
      final Element httpListener = getHttpListener(flow);
      migrateResponse(httpListener.getChild("response", HTTP_NAMESPACE));
      migrateErrorResponse(httpListener.getChild("error-response", HTTP_NAMESPACE));
    }
  }

  private void migrateResponse(Element response) {
    migrateResponse(response, "200");
  }

  private void migrateErrorResponse(Element response) {
    migrateResponse(response, "500");

    final Element body = response.getChild("body", response.getNamespace());
    if (body == null) {
      final Element newBody = new Element("body", response.getNamespace());
      newBody.setText(buildExpression("payload"));
      response.addContent(0, newBody);
    }
  }

  private void migrateResponse(Element response, String defaultStatusCode) {
    final Attribute statusCode = response.getAttribute(STATUS_CODE_ATTR_NAME);
    if (statusCode != null) {
      final String currentValue = getExpressionValue(statusCode.getValue());
      final String newValue = "vars.httpStatus default " + (isNullOrEmpty(currentValue) ? defaultStatusCode : currentValue);
      response.setAttribute(STATUS_CODE_ATTR_NAME, buildExpression(newValue));
    } else {
      response.setAttribute(STATUS_CODE_ATTR_NAME, buildExpression("vars.httpStatus default " + defaultStatusCode));
    }

    final Element header = response.getChild("headers", HTTP_NAMESPACE);
    if (header != null) {
      final String headerValue = getExpressionValue(header.getValue());
      final String newHeaderValue = "vars.outboundHeaders default {}" + (isNullOrEmpty(headerValue) ? "" : " ++ " + headerValue);
      header.setText(buildExpression(newHeaderValue));
    } else {
      final Element newHeader = new Element("headers", HTTP_NAMESPACE);
      newHeader.setText(buildExpression("vars.outboundHeaders default {}"));
      response.addContent(newHeader);
    }
  }


  private static boolean isHttpListener(Element element) {
    return HTTP_NAMESPACE.equals(element.getNamespace()) && "listener".equalsIgnoreCase(element.getName());
  }

  public static Element getHttpListener(Element flow) {
    return flow.getChildren().stream().filter(ApikitHttpListenerMapping::isHttpListener).findFirst().orElse(null);
  }

  private static Element getParentFlow(Element element) {
    if (isTopLevelElement(element))
      return null;

    final Element parent = element.getParentElement();

    if ("flow".equalsIgnoreCase(parent.getName()))
      return parent;

    return getParentFlow(parent);
  }

  private static String getExpressionValue(String str) {
    return str.replaceAll("#", "").replaceAll("\\[", "").replaceAll("]", "").trim();
  }

  private static String buildExpression(String value) {
    return "#[" + value + "]";
  }

}
