/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.federation;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.FEDERATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.FederationBadResponseOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.FederationForbiddenErrorOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.FederationInvalidTokenOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.FederationNotAuthorizedConnectionErrorOnErrorContinueElementWriter;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate federation validate elements
 *
 * @author Mulesoft Inc.
 */
public abstract class AbstractFederationValidateMigrationStep extends PolicyMigrationStep {

  protected static final String VALIDATE_TAG_NAME = "validate";
  private static final String THROW_ON_UNACCEPTED_ATTR_NAME = "throwOnUnaccepted";
  private static final String ON_UNACCEPTED_ATTR_NAME = "onUnaccepted";

  private static final String SUB_FLOW_TAG_NAME = "sub-flow";

  private static final String ACCESS_TOKEN_ATTR_NAME = "accessToken";
  private static final String ACCESS_TOKEN_ATTR_VALUE = "#[attributes.queryParams['access_token']]";
  private static final String AUTHORIZATION_ATTR_NAME = "authorization";
  private static final String AUTHORIZATION_ATTR_VALUE = "#[attributes.headers['authorization']]";
  protected static final String CONFIG_REF_ATTR_NAME = "config-ref";
  private static final String CONFIG_VALUE = "config";

  private static final String SET_VARIABLE_TAG_NAME = "set-variable";
  private static final String VARIABLE_NAME_ATTR_NAME = "variableName";
  private static final String VARIABLE_NAME_ATTR_VALUE = "federationProperties";
  private static final String VALUE_ATTR_NAME = "value";
  private static final String VALUE_ATTR_VALUE = "#[authentication.properties.userProperties]";

  private static final String OPERATION_TAG_NAME = "operation";

  private static final String ADD_REQUEST_HEADERS_TAG_NAME = "add-request-headers";
  private static final String HEADERS_TAG_NAME = "headers";
  private static final String HEADERS_CONTENT_VALUE =
      "#[vars.federationProperties filterObject ( not ($ is Object or $ is Array)) mapObject ((\"X-AGW-\" ++ ($$ replace ' ' with '-')):$)]";

  private static final String FEDERATION_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/federation http://www.mulesoft.org/schema/mule/federation/current/mule-federation.xsd";
  private static final String HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/http-policy-transform http://www.mulesoft.org/schema/mule/http-policy-transform/current/mule-http-policy-transform.xsd";

  public AbstractFederationValidateMigrationStep(Namespace namespace, String tagName) {
    super(namespace, tagName);
  }

  private void removeProcessorChainElement(Element rootElement, String onUnacceptedName) {
    Element processorChainElement = rootElement.getChildren(SUB_FLOW_TAG_NAME, MULE_4_POLICY_NAMESPACE).stream()
        .filter(element -> element.getAttributeValue(NAME_ATTR_NAME).equals(onUnacceptedName)).findFirst().orElse(null);
    if (processorChainElement != null) {
      processorChainElement.detach();
    }
  }

  private void setConfigElement(Element rootElement, Element validateElement, List<Attribute> attributes) {
    Element oauth2ConfigElement =
        new Element(getConfigElementTagName(), FEDERATION_NAMESPACE).setAttribute(NAME_ATTR_NAME, CONFIG_VALUE);
    attributes.forEach(attr -> oauth2ConfigElement.setAttribute(attr.clone()));
    new ArrayList<>(attributes).forEach(attr -> validateElement.removeAttribute(attr));
    rootElement.addContent(oauth2ConfigElement);
  }

  private void setBeforeElements(Element validateElement) {
    validateElement.setName(getAuthenticateElementTagName());
    validateElement.setNamespace(FEDERATION_NAMESPACE);
    validateElement.setAttribute(ACCESS_TOKEN_ATTR_NAME, ACCESS_TOKEN_ATTR_VALUE);
    validateElement.setAttribute(AUTHORIZATION_ATTR_NAME, AUTHORIZATION_ATTR_VALUE);
    validateElement.setAttribute(CONFIG_REF_ATTR_NAME, CONFIG_VALUE);
    validateElement.getParentElement().addContent(new Element(SET_VARIABLE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
        .setAttribute(VARIABLE_NAME_ATTR_NAME, VARIABLE_NAME_ATTR_VALUE).setAttribute(VALUE_ATTR_NAME, VALUE_ATTR_VALUE));
  }

  private void addOnErrorContinueElements(Element errorHandlerElement) {
    new FederationNotAuthorizedConnectionErrorOnErrorContinueElementWriter(getHeadersAuthenticateContent())
        .create(errorHandlerElement, true);
    new FederationInvalidTokenOnErrorContinueElementWriter().create(errorHandlerElement, true);
    new FederationForbiddenErrorOnErrorContinueElementWriter().create(errorHandlerElement, true);
    new FederationBadResponseOnErrorContinueElementWriter().create(errorHandlerElement, true);
  }

  private void completeTryElementWithOperationContent(Element tryElement) {
    Element errorHandlerElement = tryElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    if (errorHandlerElement == null) {
      errorHandlerElement = new Element(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      addOnErrorContinueElements(errorHandlerElement);
      tryElement.addContent(errorHandlerElement);
    } else {
      addOnErrorContinueElements(errorHandlerElement);
    }
  }

  private void completeSourceElement(Element sourceElement) {
    Element tryElement = sourceElement.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    if (tryElement == null) {
      tryElement = new Element(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      final List<Content> sourceCloneContentList = detachContent(sourceElement.getContent());
      sourceElement.addContent(tryElement);
      sourceCloneContentList.forEach(tryElement::addContent);
    }
    completeTryElementWithOperationContent(tryElement);
  }

  private void addOperationElement(Element proxyElement) {
    proxyElement.addContent(new Element(OPERATION_TAG_NAME, HTTP_POLICY_NAMESPACE)
        .addContent(new Element(ADD_REQUEST_HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
            .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(HEADERS_CONTENT_VALUE)))
        .addContent(new Element(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE)));
  }

  private void addNamespaceDeclarations(Element rootElement) {
    addNamespaceDeclaration(rootElement, FEDERATION_NAMESPACE, FEDERATION_XSI_SCHEMA_LOCATION_URI);
    addNamespaceDeclaration(rootElement, HTTP_TRANSFORM_NAMESPACE, HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.removeAttribute(THROW_ON_UNACCEPTED_ATTR_NAME);
    Element rootElement = getRootElement(element);
    addNamespaceDeclarations(rootElement);
    removeProcessorChainElement(rootElement, element.getAttributeValue(ON_UNACCEPTED_ATTR_NAME));
    element.removeAttribute(ON_UNACCEPTED_ATTR_NAME);
    setConfigElement(rootElement, element, element.getAttributes());
    setBeforeElements(element);
    Element sourceElement = setUpHttpPolicy(element, false, migrationReport);
    completeSourceElement(sourceElement);
    addOperationElement(sourceElement.getParentElement());
  }

  protected abstract String getConfigElementTagName();

  protected abstract String getAuthenticateElementTagName();

  protected abstract String getHeadersAuthenticateContent();
}
