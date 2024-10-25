/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.federation;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ERROR_HANDLER_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXECUTE_NEXT_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FALSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.HEADERS_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_ERROR_CONTINUE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_UNACCEPTED;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_RESPONSE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.FEDERATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;

public abstract class AbstractFederationTestCase {

  private static final String MULE_4_TAG_NAME = "mule";

  private static final String VALIDATE_TAG_NAME = "validate";
  protected static final String TOKEN_URL_ATTR_NAME = "tokenUrl";
  protected static final String TOKEN_URL_ATTR_VALUE = "{{tokenUrl}}";
  private static final String THROW_ON_UNACCEPTED_ATTR_NAME = "throwOnUnaccepted";
  protected static final String SCOPES_ATTR_NAME = "scopes";
  protected static final String SCOPES_ATTR_VALUE = "{{scopes}}";

  private static final String ON_UNACCEPTED_ATTR_VALUE = "buildResponse";

  private static final String SUB_FLOW_TAG_NAME = "sub-flow";
  private static final String SET_PROPERTY_TAG_NAME = "set-property";
  private static final String SET_PAYLOAD_TAG_NAME = "set-payload";
  private static final String PROPERTY_NAME_ATTR_NAME = "propertyName";
  protected static final String NAME_ATTR_NAME = "name";
  private static final String HTTP_STATUS = "http.status";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String VALUE_ATTR_NAME = "value";
  private static final String VALUE_403 = "403";
  private static final String VALUE_APP_XML = "application/xml";
  private static final String VALUE_APP_JSON = "application/json";
  private static final String VALUE_SOAP_FAULT = "#[soapFault('client', flowVars._invalidClientMessage)]";
  private static final String VALUE_FLOW_VARS = "#[flowVars._invalidClientMessage]";
  private static final String STATUS_CODE_ATTR_NAME = "statusCode";
  private static final String TYPE_ATTR_NAME = "type";
  private static final String LOG_EXCEPTION_ATTR_NAME = "logException";

  private static final String ACCESS_TOKEN_ATTR_NAME = "accessToken";
  private static final String ACCESS_TOKEN_ATTR_VALUE = "#[attributes.queryParams['access_token']]";
  private static final String AUTHORIZATION_ATTR_NAME = "authorization";
  private static final String AUTHORIZATION_ATTR_VALUE = "#[attributes.headers['authorization']]";
  private static final String CONFIG_REF_ATTR_NAME = "config-ref";

  private static final String SET_VARIABLE_TAG_NAME = "set-variable";
  private static final String VARIABLE_NAME_ATTR_NAME = "variableName";
  private static final String VARIABLE_NAME_ATTR_VALUE = "federationProperties";
  private static final String VALUE_ATTR_VALUE = "#[authentication.properties.userProperties]";

  private static final String OPERATION_TAG_NAME = "operation";
  private static final String ADD_REQUEST_HEADERS_TAG_NAME = "add-request-headers";
  private static final String OPERATION_HEADERS_CONTENT =
      "#[vars.federationProperties filterObject ( not ($ is Object or $ is Array)) mapObject ((\"X-AGW-\" ++ ($$ replace ' ' with '-')):$)]";

  private static final String TYPE_FEDERATION_BAD_RESPONSE_ERROR = "FEDERATION:BAD_RESPONSE_ERROR";
  private static final String STATUS_CODE_500 = "500";
  private static final String TYPE_FEDERATION_FORBIDDEN_ERROR = "FEDERATION:FORBIDDEN_ERROR";
  private static final String STATUS_CODE_403 = "403";
  private static final String TYPE_FEDERATION_INVALID_TOKEN = "FEDERATION:INVALID_TOKEN";
  private static final String STATUS_CODE_400 = "400";
  private static final String TYPE_FEDERATION_NOT_AUTHORIZED_CONNECTION_ERROR =
      "FEDERATION:NOT_AUTHORIZED, FEDERATION:CONNECTION_ERROR";
  private static final String STATUS_CODE_401 = "401";

  private static final String BODY_TAG_NAME = "body";
  private static final String DW_BODY_RESEPONSE_VALUE =
      "#[ output application/json --- {\"error\": \"$(error.description)\"} ]";

  protected static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/federation/expected");

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    amb.withPom(APPLICATION_MODEL_PATH.resolve("pom.xml"));
    appModel = amb.build();
  }

  private Element getSetPropertyElement(String propertyAttrValue, String valueAttrValue) {
    return new Element(SET_PROPERTY_TAG_NAME, MULE_4_POLICY_NAMESPACE)
        .setAttribute(PROPERTY_NAME_ATTR_NAME, propertyAttrValue)
        .setAttribute(VALUE_ATTR_NAME, valueAttrValue);
  }

  private Element getProcessorChainElement() {
    return new Element(SUB_FLOW_TAG_NAME, MULE_4_POLICY_NAMESPACE)
        .setAttribute(NAME_ATTR_NAME, ON_UNACCEPTED_ATTR_VALUE)
        .addContent(getSetPropertyElement(HTTP_STATUS, VALUE_403))
        .addContent(getSetPropertyElement(CONTENT_TYPE, VALUE_APP_XML))
        .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE)
            .setAttribute(VALUE_ATTR_NAME, VALUE_SOAP_FAULT))
        .addContent(getSetPropertyElement(CONTENT_TYPE, VALUE_APP_JSON))
        .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE)
            .setAttribute(VALUE_ATTR_NAME, VALUE_FLOW_VARS));
  }

  protected Element getTestElement() {
    Element testElement = new Element(VALIDATE_TAG_NAME, getTestElementNamespace())
        .setAttribute(TOKEN_URL_ATTR_NAME, TOKEN_URL_ATTR_VALUE)
        .setAttribute(THROW_ON_UNACCEPTED_ATTR_NAME, TRUE)
        .setAttribute(ON_UNACCEPTED, ON_UNACCEPTED_ATTR_VALUE);
    Element root = new Element(MULE_4_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    root.addNamespaceDeclaration(MULE_4_POLICY_NAMESPACE);
    new Document().setRootElement(root.addContent(getProcessorChainElement()).addContent(testElement));
    return testElement;
  }

  protected Element getRootElement(final Element element) {
    Document doc = element.getDocument();
    if (doc != null) {
      return doc.getRootElement();
    }
    return null;
  }

  private void assertProcessorChain(Element element) {
    Element root = getRootElement(element);
    assertThat(root.getChild(SUB_FLOW_TAG_NAME, MULE_4_POLICY_NAMESPACE), nullValue());
  }

  private void assertSetResponseElement(Element setResponseElement, String expectedStatusCodeValue) {
    assertThat(setResponseElement, notNullValue());
    assertThat(setResponseElement.getAttributeValue(STATUS_CODE_ATTR_NAME), is(expectedStatusCodeValue));
    Element bodyElement = setResponseElement.getChild(BODY_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(bodyElement, notNullValue());
    assertThat(bodyElement.getContentSize(), is(1));
    assertThat(bodyElement.getContent(0).getValue(), is(DW_BODY_RESEPONSE_VALUE));
  }

  private void assertBadResponseElement(Element badResponseElement) {
    assertThat(badResponseElement, notNullValue());
    assertThat(badResponseElement.getAttributeValue(TYPE_ATTR_NAME), is(TYPE_FEDERATION_BAD_RESPONSE_ERROR));
    assertThat(badResponseElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertSetResponseElement(badResponseElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE), STATUS_CODE_500);
  }

  private void asssertForbiddenErrorElement(Element forbiddenErrorElement) {
    assertThat(forbiddenErrorElement, notNullValue());
    assertThat(forbiddenErrorElement.getAttributeValue(TYPE_ATTR_NAME), is(TYPE_FEDERATION_FORBIDDEN_ERROR));
    assertThat(forbiddenErrorElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertSetResponseElement(forbiddenErrorElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE), STATUS_CODE_403);
  }

  private void assertInvalidTokenElement(Element invalidTokenElement) {
    assertThat(invalidTokenElement, notNullValue());
    assertThat(invalidTokenElement.getAttributeValue(TYPE_ATTR_NAME), is(TYPE_FEDERATION_INVALID_TOKEN));
    assertThat(invalidTokenElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertSetResponseElement(invalidTokenElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE), STATUS_CODE_400);
  }

  private void assertNotAuthorizedConnectionErrorElement(Element notAuthorizedConnectionErrorElement) {
    assertThat(notAuthorizedConnectionErrorElement, notNullValue());
    assertThat(notAuthorizedConnectionErrorElement.getAttributeValue(TYPE_ATTR_NAME),
               is(TYPE_FEDERATION_NOT_AUTHORIZED_CONNECTION_ERROR));
    assertThat(notAuthorizedConnectionErrorElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    Element setResponseElement = notAuthorizedConnectionErrorElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertSetResponseElement(setResponseElement, STATUS_CODE_401);
    Element headersElement = setResponseElement.getChild(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(headersElement, notNullValue());
    assertThat(headersElement.getContentSize(), is(1));
    assertThat(headersElement.getContent(0).getValue(), is(getExpectedHeadersAuthenticateContent()));
  }


  private void assertProxyElement(Element element) {
    Element root = getRootElement(element);
    Element proxyElement = root.getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(proxyElement, notNullValue());
    assertThat(proxyElement.getContentSize(), is(2));
    Element sourceElement = proxyElement.getChild(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(sourceElement, notNullValue());
    assertThat(sourceElement.getContentSize(), is(1));
    Element tryElement = sourceElement.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(tryElement, notNullValue());
    Element executeNextElement = tryElement.getChild(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(executeNextElement, notNullValue());
    Element errorHandlerElement = tryElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(errorHandlerElement, notNullValue());
    List<Element> onErrorContinueElements =
        errorHandlerElement.getChildren(ON_ERROR_CONTINUE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(onErrorContinueElements, notNullValue());
    assertThat(onErrorContinueElements.size(), is(4));
    assertNotAuthorizedConnectionErrorElement(onErrorContinueElements.get(0));
    assertInvalidTokenElement(onErrorContinueElements.get(1));
    asssertForbiddenErrorElement(onErrorContinueElements.get(2));
    assertBadResponseElement(onErrorContinueElements.get(3));
  }

  private void assertAuthenticateElement(Element element) {
    assertThat(element.getName(), is(getExpectedAuthenticateTagName()));
    assertThat(element.getNamespace(), is(FEDERATION_NAMESPACE));
    assertThat(element.getAttributes().size(), is(3));
    assertThat(element.getAttributeValue(ACCESS_TOKEN_ATTR_NAME), is(ACCESS_TOKEN_ATTR_VALUE));
    assertThat(element.getAttributeValue(AUTHORIZATION_ATTR_NAME), is(AUTHORIZATION_ATTR_VALUE));
    assertThat(element.getAttributeValue(CONFIG_REF_ATTR_NAME), is(CONFIG));
  }

  private void assertSetVariableElement(Element element) {
    Element setVariableElement = element.getParentElement().getChild(SET_VARIABLE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(setVariableElement, notNullValue());
    assertThat(setVariableElement.getAttributes().size(), is(2));
    assertThat(setVariableElement.getAttributeValue(VARIABLE_NAME_ATTR_NAME), is(VARIABLE_NAME_ATTR_VALUE));
    assertThat(setVariableElement.getAttributeValue(VALUE_ATTR_NAME), is(VALUE_ATTR_VALUE));
  }

  private void assertOperationElement(Element element) {
    Element operationElement = getRootElement(element).getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE)
        .getChild(OPERATION_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(operationElement, notNullValue());
    assertThat(operationElement.getContentSize(), is(2));
    Element addRequestHeadersElement = operationElement.getChild(ADD_REQUEST_HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(addRequestHeadersElement, notNullValue());
    assertThat(addRequestHeadersElement.getAttributes().size(), is(0));
    assertThat(addRequestHeadersElement.getContentSize(), is(1));
    Element headersElement = addRequestHeadersElement.getChild(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(headersElement, notNullValue());
    assertThat(headersElement.getContentSize(), is(1));
    assertThat(headersElement.getContent(0).getValue(), is(OPERATION_HEADERS_CONTENT));
    Element executeNextElement = operationElement.getChild(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(executeNextElement, notNullValue());
  }

  protected void doAsserts(Element element) {
    assertAuthenticateElement(element);
    assertSetVariableElement(element);
    assertProcessorChain(element);
    assertProxyElement(element);
    assertOperationElement(element);
  }

  protected abstract Namespace getTestElementNamespace();

  protected abstract String getExpectedAuthenticateTagName();

  protected abstract String getExpectedHeadersAuthenticateContent();
}
