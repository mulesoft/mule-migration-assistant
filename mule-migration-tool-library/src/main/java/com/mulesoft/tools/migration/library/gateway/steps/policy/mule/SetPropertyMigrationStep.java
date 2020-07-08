/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.mule;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate set-property element
 *
 * @author Mulesoft Inc.
 */
public class SetPropertyMigrationStep extends PolicyMigrationStep {

  private static final String POLICY_TAG_NAME = "policy";
  private static final String SET_PROPERTY_TAG_NAME = "set-property";

  private static final String OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME = "outbound-properties-to-var";
  private static final String SET_RESPONSE_TAG_NAME = "set-response";
  private static final String STATUS_CODE_ATTR_NAME = "statusCode";
  private static final String HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE_DWL =
      "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]";
  private static final String HEADERS_TAG_NAME = "headers";
  private static final String HTTP_LISTENER_RESPONSE_HEADERS_DWL =
      "#[migration::HttpListener::httpListenerResponseHeaders(vars)]";

  private static final String COMPATIBILITY_XSI_SCHEMA_LOCATION =
      "http://www.mulesoft.org/schema/mule/compatibility http://www.mulesoft.org/schema/mule/compatibility/current/mule-compatibility.xsd";
  private static final String HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/http-policy-transform http://www.mulesoft.org/schema/mule/http-policy-transform/current/mule-http-policy-transform.xsd";

  public SetPropertyMigrationStep() {
    super(COMPATIBILITY_NAMESPACE, SET_PROPERTY_TAG_NAME);
  }

  private boolean hasElement(Element element, String targetTagName, Namespace targetNamespace) {
    return element.getChild(targetTagName, targetNamespace) != null;
  }

  private void addNamespaces(Element rootElement) {
    addNamespaceDeclaration(rootElement, HTTP_TRANSFORM_NAMESPACE, HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI);
    addNamespaceDeclaration(rootElement, COMPATIBILITY_NAMESPACE, COMPATIBILITY_XSI_SCHEMA_LOCATION);
  }

  private void addOutboundPropertiesElement(Element parentElement, boolean hasSetResponseElement) {
    if (hasSetResponseElement) {
      parentElement.addContent(getElementPosition(parentElement, SET_RESPONSE_TAG_NAME) - 1,
                               new Element(OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME, COMPATIBILITY_NAMESPACE));
    } else {
      parentElement.addContent(new Element(OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME, COMPATIBILITY_NAMESPACE));
    }
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    if (getRootElement(element).getName().equals(POLICY_TAG_NAME)) {
      addNamespaces(getRootElement(element));
      Element parentElement = element.getParentElement();
      boolean hasSetResponseElement = hasElement(parentElement, SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
      if (!hasElement(parentElement, OUTBOUND_PROPERTIES_TO_VAR_TAG_NAME, COMPATIBILITY_NAMESPACE)) {
        addOutboundPropertiesElement(parentElement, hasSetResponseElement);
      }
      if (!hasSetResponseElement) {
        parentElement.addContent(
                                 new Element(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
                                     .setAttribute(STATUS_CODE_ATTR_NAME, HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE_DWL)
                                     .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
                                         .addContent(HTTP_LISTENER_RESPONSE_HEADERS_DWL)));
        com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.httpListenerLib(getApplicationModel());
        new HttpTransformPomContributionMigrationStep().execute(getApplicationModel().getPomModel().get(), migrationReport);
        com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.httpListenerLib(getApplicationModel());
      }
    }
  }
}
