/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.API_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.AUTO;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG_REF;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.DISABLE_VALIDATIONS_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.DISABLE_VALIDATIONS_ATTR_VALUE_3X;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EE_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ERROR_HANDLER_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FALSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FLOW_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.HEADERS_STRICT_VALIDATION_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.LOG_EXCEPTION_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_ERROR_CONTINUE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_ERROR_PROPAGATE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PARSER_ATTR_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.RAML;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.RAML_ATTR_VALUE_3X;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.RAML_ATTR_VALUE_3X_HC;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.RAML_LOCATION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.RAML_PROXY_CONFIG_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.REST_VALIDATOR_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_ATTRIBUTES_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_PAYLOAD_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_VARIABLE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.STATUS_CODE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TYPE_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALIDATE_REQUEST_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALIDATION_DISABLE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RamlTagMigrationStepTestCase {

  private static final String CONFIG_REF_ATTR_VALUE = "proxy-config";

  private static final String REF_ATTR_NAME = "ref";
  private static final String NAME_ATTR_VALUE = "defaultExceptionStrategy";
  private static final String UNBOUNDED_NAME_ATTR_VALUE = "unboundDefaultExceptionStrategy";

  private static final String VARIABLE_NAME_ATTR_NAME = "variableName";
  private static final String VARIABLE_NAME_ATTR_VALUE = "httpStatus";

  private static final String APIKIT_TYPE_NOT_FOUND = "APIKIT:NOT_FOUND";
  private static final String STATUS_CODE_404 = "404";
  private static final String SET_PAYLOAD_VALUE_ATTR_VALUE_404 = "resource not found";
  private static final String APIKIT_TYPE_METHOD_NOT_ALLOWED = "APIKIT:METHOD_NOT_ALLOWED";
  private static final String STATUS_CODE_405 = "405";
  private static final String SET_PAYLOAD_VALUE_ATTR_VALUE_405 = "method not allowed";
  private static final String WHEN_ATTR_NAME = "when";
  private static final String TIMEOUT_EXCEPTION_MEL = "#[mel:exception.causedBy(java.util.concurrent.TimeoutException)]";
  private static final String STATUS_CODE_504 = "504";
  private static final String SET_PAYLOAD_VALUE_ATTR_VALUE_504 = "Gateway timeout";

  private static final String HTTP_TIMEOUT_TYPE = "HTTP:TIMEOUT";
  private static final String STATUS_CODE_504_4X = "{ " + STATUS_CODE + ": " + STATUS_CODE_504 + " }";
  private static final String REST_VALIDATOR_BAD_REQUEST_TYPE = "REST-VALIDATOR:BAD_REQUEST";
  private static final String STATUS_CODE_400_4X = "{ " + STATUS_CODE + ": 400 }";
  private static final String REST_VALIDATOR_METHOD_NOT_ALLOWED_TYPE = "REST-VALIDATOR:METHOD_NOT_ALLOWED";
  private static final String STATUS_CODE_405_4X = "{ " + STATUS_CODE + ": " + STATUS_CODE_405 + " }";
  private static final String REST_VALIDATOR_RESOURCE_NOT_FOUND_TYPE = "REST-VALIDATOR:RESOURCE_NOT_FOUND";
  private static final String STATUS_CODE_404_4X = "{ " + STATUS_CODE + ": " + STATUS_CODE_404 + " }";
  private static final String SET_PAYLOAD_VALUE_4X = "output application/json --- {\"error\": \"$(error.description)\"}";

  private static final String TRANSFORM_TAG_NAME = "transform";
  private static final String MESSAGE_TAG_NAME = "message";

  private MigrationReport reportMock;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
  }

  private Element getTestElement(boolean hardCodedProperties) {
    Element ramlElement = new Element(RAML, PROXY_NAMESPACE).setAttribute(CONFIG_REF, CONFIG_REF_ATTR_VALUE);
    Element muleElement = new Element(MULE_TAG_NAME, MULE_4_NAMESPACE)
        .addContent(new Element(FLOW_TAG_NAME, MULE_4_NAMESPACE)
            .addContent(ramlElement))
        .addContent(new Element(RAML_PROXY_CONFIG_TAG_NAME, PROXY_NAMESPACE)
            .setAttribute(NAME, CONFIG_REF_ATTR_VALUE)
            .setAttribute(RAML, hardCodedProperties ? RAML_ATTR_VALUE_3X_HC : RAML_ATTR_VALUE_3X)
            .setAttribute(DISABLE_VALIDATIONS_ATTR_NAME, hardCodedProperties ? FALSE : DISABLE_VALIDATIONS_ATTR_VALUE_3X));
    new Document().setRootElement(muleElement);
    return ramlElement;
  }

  private Element addErrorHandlerTestElement(Element flowElement, boolean unbounded) {
    flowElement.addContent(new Element(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
        .setAttribute(REF_ATTR_NAME, NAME_ATTR_VALUE));
    Element errorHandlerElementWithExceptions = new Element(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
        .setAttribute(NAME, unbounded ? UNBOUNDED_NAME_ATTR_VALUE : NAME_ATTR_VALUE);
    flowElement.getParentElement()
        .addContent(errorHandlerElementWithExceptions);
    return errorHandlerElementWithExceptions;
  }

  private Attribute getOnErrorPropagateAttribute(boolean isTimeoutError, String attributeValue) {
    return new Attribute(isTimeoutError ? WHEN_ATTR_NAME : TYPE_ATTR_NAME, attributeValue);
  }

  private void addOnErrorPropagateElement(Element errorHandlerElement, boolean isTimeoutError, String errorType,
                                          String statusCode,
                                          String payloadValue) {
    errorHandlerElement.addContent(
                                   new Element(ON_ERROR_PROPAGATE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
                                       .setAttribute(getOnErrorPropagateAttribute(isTimeoutError, errorType))
                                       .addContent(new Element(SET_VARIABLE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
                                           .setAttribute(VALUE_ATTR_NAME, statusCode)
                                           .setAttribute(VARIABLE_NAME_ATTR_NAME, VARIABLE_NAME_ATTR_VALUE))
                                       .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
                                           .setAttribute(VALUE_ATTR_NAME, payloadValue)));
  }

  private void add404OnErrorPropagateElement(Element errorHandlerElement) {
    addOnErrorPropagateElement(errorHandlerElement, false, APIKIT_TYPE_NOT_FOUND, STATUS_CODE_404,
                               SET_PAYLOAD_VALUE_ATTR_VALUE_404);
  }

  private void add405OnErrorPropagateElement(Element errorHandlerElement) {
    addOnErrorPropagateElement(errorHandlerElement, false, APIKIT_TYPE_METHOD_NOT_ALLOWED, STATUS_CODE_405,
                               SET_PAYLOAD_VALUE_ATTR_VALUE_405);
  }

  private void add504OnErrorPropagateElement(Element errorHandlerElement) {
    addOnErrorPropagateElement(errorHandlerElement, true, TIMEOUT_EXCEPTION_MEL, STATUS_CODE_504,
                               SET_PAYLOAD_VALUE_ATTR_VALUE_504);
  }

  @Test
  public void migrateRamlTagParametrized() {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(false);

    step.execute(ramlElement, reportMock);

    assertFullRamlMigration(ramlElement, false);
  }

  @Test
  public void migrateRamlTagHardcoded() {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(true);

    step.execute(ramlElement, reportMock);

    assertFullRamlMigration(ramlElement, true);
  }

  @Test
  public void ramlTagWithUnboundConfigThrowsException() throws RuntimeException {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(false).setAttribute(CONFIG_REF, "otherConfig");
    thrown.expectMessage("No matching config was found for RAML element.");
    thrown.expect(RuntimeException.class);

    try {
      step.execute(ramlElement, reportMock);
    } catch (Exception e) {
      verify(reportMock).report("raml.noMatchingConfig", ramlElement, ramlElement);
      throw e;
    }
  }

  @Test
  public void migrateRamlWithFullErrorsInErrorHandler() {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(false);

    Element errorHandlerElement = addErrorHandlerTestElement(ramlElement.getParentElement(), false);
    add404OnErrorPropagateElement(errorHandlerElement);
    add405OnErrorPropagateElement(errorHandlerElement);
    add504OnErrorPropagateElement(errorHandlerElement);

    step.execute(ramlElement, reportMock);

    assertFullRamlMigration(ramlElement, false);
    List<Element> onErrorContinueElements = assertFlowWithErrorHandler(ramlElement);
    assertOnErrorContinueElements(onErrorContinueElements);
  }

  @Test
  public void migrateRamlWithIncompleteErrorsInErrorHandler() {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(false);

    Element errorHandlerElement = addErrorHandlerTestElement(ramlElement.getParentElement(), false);
    add404OnErrorPropagateElement(errorHandlerElement);

    step.execute(ramlElement, reportMock);

    assertFullRamlMigration(ramlElement, false);
    List<Element> onErrorContinueElements = assertFlowWithErrorHandler(ramlElement);
    assertOnErrorContinueElements(onErrorContinueElements);
    verify(reportMock).report("raml.autocompletedOnErrorContinueElement", onErrorContinueElements.get(2),
                              onErrorContinueElements.get(2),
                              REST_VALIDATOR_METHOD_NOT_ALLOWED_TYPE);
    verify(reportMock).report("raml.autocompletedOnErrorContinueElement", onErrorContinueElements.get(3),
                              onErrorContinueElements.get(3),
                              HTTP_TIMEOUT_TYPE);
  }

  @Test
  public void migrateRamlWithNoErrorsInErrorHandler() {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(false);

    addErrorHandlerTestElement(ramlElement.getParentElement(), false);

    step.execute(ramlElement, reportMock);

    assertFullRamlMigration(ramlElement, false);
    List<Element> onErrorContinueElements = assertFlowWithErrorHandler(ramlElement);
    assertOnErrorContinueElements(onErrorContinueElements);
    verify(reportMock).report("raml.autocompletedOnErrorContinueElement", onErrorContinueElements.get(1),
                              onErrorContinueElements.get(1),
                              REST_VALIDATOR_RESOURCE_NOT_FOUND_TYPE);
    verify(reportMock).report("raml.autocompletedOnErrorContinueElement", onErrorContinueElements.get(2),
                              onErrorContinueElements.get(2),
                              REST_VALIDATOR_METHOD_NOT_ALLOWED_TYPE);
    verify(reportMock).report("raml.autocompletedOnErrorContinueElement", onErrorContinueElements.get(3),
                              onErrorContinueElements.get(3),
                              HTTP_TIMEOUT_TYPE);
  }

  @Test
  public void migrateRamlTagWithUnboundErrorHandler() {
    RamlTagMigrationStep step = new RamlTagMigrationStep();
    Element ramlElement = getTestElement(false);

    Element errorHandlerElement = addErrorHandlerTestElement(ramlElement.getParentElement(), true);
    add404OnErrorPropagateElement(errorHandlerElement);
    add405OnErrorPropagateElement(errorHandlerElement);
    add504OnErrorPropagateElement(errorHandlerElement);

    step.execute(ramlElement, reportMock);

    assertFullRamlMigration(ramlElement, false);
    Element rootElement = ramlElement.getDocument().getRootElement();
    assertThat(rootElement.getContentSize(), is(3));
    Element unmigratedErrorHandlerElement = rootElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(unmigratedErrorHandlerElement, notNullValue());
    assertThat(unmigratedErrorHandlerElement.getContentSize(), is(3));
    assertThat(unmigratedErrorHandlerElement.getChildren().get(0).getName(), is(ON_ERROR_PROPAGATE_TAG_NAME));
    assertThat(unmigratedErrorHandlerElement.getChildren().get(0).getAttributeValue(TYPE_ATTR_NAME), is(APIKIT_TYPE_NOT_FOUND));
    assertThat(unmigratedErrorHandlerElement.getChildren().get(1).getName(), is(ON_ERROR_PROPAGATE_TAG_NAME));
    assertThat(unmigratedErrorHandlerElement.getChildren().get(1).getAttributeValue(TYPE_ATTR_NAME),
               is(APIKIT_TYPE_METHOD_NOT_ALLOWED));
    assertThat(unmigratedErrorHandlerElement.getChildren().get(2).getName(), is(ON_ERROR_PROPAGATE_TAG_NAME));
    assertThat(unmigratedErrorHandlerElement.getChildren().get(2).getAttributeValue(WHEN_ATTR_NAME), is(TIMEOUT_EXCEPTION_MEL));
  }

  private void assertFullRamlMigration(Element ramlElement, boolean hardCodedProperties) {
    assertValidateRequestTag(ramlElement);
    Element configElement = ramlElement.getDocument().getRootElement().getChild(CONFIG, REST_VALIDATOR_NAMESPACE);
    assertConfigElement(configElement, hardCodedProperties);
    verify(reportMock).report("raml.autocompletedConfigAttribute", configElement, configElement,
                              PARSER_ATTR_VALUE, AUTO);
    verify(reportMock).report("raml.autocompletedConfigAttribute", configElement, configElement,
                              QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME, FALSE);
    verify(reportMock).report("raml.autocompletedConfigAttribute", configElement, configElement,
                              HEADERS_STRICT_VALIDATION_ATTR_NAME, FALSE);
  }

  private void assertValidateRequestTag(Element ramlElement) {
    assertThat(ramlElement.getName(), is(VALIDATE_REQUEST_TAG_NAME));
    assertThat(ramlElement.getNamespace(), is(REST_VALIDATOR_NAMESPACE));
    assertThat(ramlElement.getAttributes().size(), is(1));
    assertThat(ramlElement.getAttributeValue(CONFIG_REF), is("rest-validator-config"));
  }

  private void assertConfigElement(Element configElement, boolean hardCodedProperties) {
    assertThat(configElement, notNullValue());
    assertThat(configElement.getAttributeValue(NAME), is("rest-validator-config"));
    assertThat(configElement.getAttributeValue(API_ATTR_NAME),
               is(hardCodedProperties ? RAML_ATTR_VALUE_3X_HC : "${" + RAML_LOCATION + "}"));
    assertThat(configElement.getAttributeValue(PARSER_ATTR_VALUE), is(AUTO));
    assertThat(configElement.getAttributeValue(DISABLE_VALIDATIONS_ATTR_NAME),
               is(hardCodedProperties ? FALSE : "${" + VALIDATION_DISABLE + "}"));
    assertThat(configElement.getAttributeValue(QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME),
               is(FALSE));
    assertThat(configElement.getAttributeValue(HEADERS_STRICT_VALIDATION_ATTR_NAME),
               is(FALSE));
  }

  private List<Element> assertFlowWithErrorHandler(Element ramlElement) {
    Element rootElement = ramlElement.getDocument().getRootElement();
    assertThat(rootElement.getContentSize(), is(2));
    Element flowElement = rootElement.getChild(FLOW_TAG_NAME, MULE_4_NAMESPACE);
    assertThat(flowElement, notNullValue());
    assertThat(flowElement.getContentSize(), is(2));
    Element errorHandlerElement = flowElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_NAMESPACE);
    assertThat(errorHandlerElement, notNullValue());
    return errorHandlerElement.getChildren(ON_ERROR_CONTINUE_TAG_NAME, MULE_4_NAMESPACE);
  }

  private void assertOnErrorContinueElements(List<Element> onErrorContinueElements) {
    assertThat(onErrorContinueElements.size(), is(4));
    assertBadRequest(onErrorContinueElements.get(0));
    assertResourceNotFound(onErrorContinueElements.get(1));
    assertMethodNotAllowed(onErrorContinueElements.get(2));
    assertHTTPTimeout(onErrorContinueElements.get(3));
  }

  private void assertBadRequest(Element onErrorContinueElement) {
    assertOnErrorContinueElement(onErrorContinueElement, REST_VALIDATOR_BAD_REQUEST_TYPE, true, STATUS_CODE_400_4X);
    verify(reportMock).report("raml.autocompletedOnErrorContinueElement", onErrorContinueElement, onErrorContinueElement,
                              REST_VALIDATOR_BAD_REQUEST_TYPE);
  }

  private void assertMethodNotAllowed(Element onErrorContinueElement) {
    assertOnErrorContinueElement(onErrorContinueElement, REST_VALIDATOR_METHOD_NOT_ALLOWED_TYPE, true, STATUS_CODE_405_4X);
  }

  private void assertResourceNotFound(Element onErrorContinueElement) {
    assertOnErrorContinueElement(onErrorContinueElement, REST_VALIDATOR_RESOURCE_NOT_FOUND_TYPE, true, STATUS_CODE_404_4X);
  }

  private void assertHTTPTimeout(Element onErrorContinueElement) {
    assertOnErrorContinueElement(onErrorContinueElement, HTTP_TIMEOUT_TYPE, false, STATUS_CODE_504_4X);
  }

  private void assertOnErrorContinueElement(Element onErrorContinueElement, String expectedType, boolean expectSetPayloadElement,
                                            String expectedStatusCode) {
    assertThat(onErrorContinueElement.getName(), is(ON_ERROR_CONTINUE_TAG_NAME));
    assertThat(onErrorContinueElement.getNamespace(), is(MULE_4_NAMESPACE));
    assertThat(onErrorContinueElement.getAttributeValue(TYPE_ATTR_NAME), is(expectedType));
    assertThat(onErrorContinueElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertThat(onErrorContinueElement.getContentSize(), is(1));
    assertTransformElement(onErrorContinueElement.getChild(TRANSFORM_TAG_NAME, EE_NAMESPACE), expectSetPayloadElement,
                           expectedStatusCode);
  }

  private void assertTransformElement(Element transformElement, boolean expectSetPayloadElement, String expectedStatusCode) {
    assertThat(transformElement, notNullValue());
    Element messageElement = transformElement.getChild(MESSAGE_TAG_NAME, EE_NAMESPACE);
    assertThat(messageElement, notNullValue());
    if (expectSetPayloadElement) {
      assertSetPayloadElement(messageElement.getChild(SET_PAYLOAD_TAG_NAME, EE_NAMESPACE));
    }
    assertSetAttributesElement(messageElement.getChild(SET_ATTRIBUTES_TAG_NAME, EE_NAMESPACE), expectedStatusCode);
  }

  private void assertSetPayloadElement(Element setPayloadElement) {
    assertThat(setPayloadElement, notNullValue());
    assertThat(setPayloadElement.getText(), is(SET_PAYLOAD_VALUE_4X));
  }

  private void assertSetAttributesElement(Element setAttributesElement, String expectedStatusCode) {
    assertThat(setAttributesElement, notNullValue());
    assertThat(setAttributesElement.getText(), is(expectedStatusCode));
  }

}
