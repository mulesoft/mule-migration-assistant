/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.clientidenforcement;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CLIENT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CLIENT_ID_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CLIENT_SECRET;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CLIENT_SECRET_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG_REF;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONTENT_TYPE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ERROR_HANDLER_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXECUTE_NEXT_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FALSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.HTTP_STATUS;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.LOG_EXCEPTION_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MIGRATION_RESOURCES_PATH;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_ERROR_CONTINUE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ON_UNACCEPTED;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_ID_BUILD_RESPONSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROPERTY_NAME_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_PAYLOAD_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_PROPERTY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SUB_FLOW_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TYPE_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALIDATE_CLIENT_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_403;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_APP_JSON;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_APP_XML;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_ATTR_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_FLOW_VARS;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VALUE_SOAP_FAULT;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_PLATFORM_GW_MULE_3_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.CLIENT_ID_ENFORCEMENT_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.library.gateway.steps.policy.clientidenforcement.ValidateClientTagMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ValidateClientTagMigrationStepTestCase {

  private static final String VALIDATE_CLIENT_ID_TAG_NAME = "validate-client-id";
  private static final String VALIDATE_BASIC_AUTH_ENCODED_CLIENT_TAG_NAME = "validate-basic-auth-encoded-client";

  private static final String ENCODED_CLIENT = "encodedClient";
  private static final String ENCODED_CLIENT_VALUE = "#[attributes.headers.authorization]";
  private static final String BASIC_AUTH_ENABLED = "basicAuthEnabled";
  private static final String CLIENT_ENFORCEMENT_CONFIG = "clientEnforcementConfig";

  private static final String VALUE_ERROR_DESCRIPTION = "#[error.description]";

  private static final String CLIENT_ID_ENFORCEMENT_TYPE =
      "CLIENT-ID-ENFORCEMENT:INVALID_API, CLIENT-ID-ENFORCEMENT:INVALID_CLIENT, CLIENT-ID-ENFORCEMENT:INVALID_CREDENTIALS";

  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/client-id-enforcement/expected");

  private MigrationReport reportMock;
  protected ApplicationModel appModel;

  private Element getTestElement() {
    return new Element(VALIDATE_CLIENT_TAG_NAME, API_PLATFORM_GW_MULE_3_NAMESPACE)
        .setAttribute(new Attribute(ON_UNACCEPTED, POLICY_ID_BUILD_RESPONSE));
  }

  private Element getSetPropertyElement(String propertyAttrValue, String valueAttrValue) {
    return new Element(SET_PROPERTY_TAG_NAME, MULE_4_POLICY_NAMESPACE)
        .setAttribute(new Attribute(PROPERTY_NAME_ATTR_NAME, propertyAttrValue))
        .setAttribute(new Attribute(VALUE_ATTR_NAME, valueAttrValue));
  }

  private Element getProcessorChainElement() {
    return new Element(SUB_FLOW_TAG_NAME, MULE_4_POLICY_NAMESPACE)
        .setAttribute(new Attribute(NAME, POLICY_ID_BUILD_RESPONSE))
        .addContent(getSetPropertyElement(HTTP_STATUS, VALUE_403))
        .addContent(getSetPropertyElement(CONTENT_TYPE, VALUE_APP_XML))
        .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE)
            .setAttribute(new Attribute(VALUE_ATTR_NAME, VALUE_SOAP_FAULT)))
        .addContent(getSetPropertyElement(CONTENT_TYPE, VALUE_APP_JSON))
        .addContent(new Element(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE)
            .setAttribute(new Attribute(VALUE_ATTR_NAME, VALUE_FLOW_VARS)));
  }

  private void addProcessorChainElement(Element element) {
    Element root = new Element(MULE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    root.addNamespaceDeclaration(MULE_4_POLICY_NAMESPACE);
    root.addContent(getProcessorChainElement()).addContent(element);
    new Document().setRootElement(root);
  }

  private Element getRootElement(final Element element) {
    Document doc = element.getDocument();
    if (doc != null) {
      return doc.getRootElement();
    }
    return null;
  }

  private void assertConfigElement(Element element) {
    Element root = getRootElement(element);
    assertThat(root.getChildren(CONFIG, CLIENT_ID_ENFORCEMENT_NAMESPACE).size(), is(1));
    Element config = root.getChild(CONFIG, CLIENT_ID_ENFORCEMENT_NAMESPACE);
    assertThat(config.getName(), is(CONFIG));
    assertThat(config.getNamespace(), is(CLIENT_ID_ENFORCEMENT_NAMESPACE));
    assertThat(config.getAttributes().size(), is(1));
    assertThat(config.getAttribute(NAME).getValue(), is(CLIENT_ENFORCEMENT_CONFIG));
  }

  private void assertDwlScript() {
    String[] resourcesFiles = appModel.getProjectBasePath().resolve(MIGRATION_RESOURCES_PATH).toFile().list();
    assertThat(requireNonNull(resourcesFiles).length, is(1));
    assertThat(requireNonNull(resourcesFiles)[0], is("HttpListener.dwl"));
  }

  private void assertProcessorChain(Element element) {
    List<Element> setPropertyElements = element.getChildren(SET_PROPERTY_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    assertThat(setPropertyElements, notNullValue());
    assertThat(setPropertyElements.size(), is(3));
    assertThat(setPropertyElements.get(0).getAttributeValue(PROPERTY_NAME_ATTR_NAME), is(HTTP_STATUS));
    assertThat(setPropertyElements.get(0).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_403));
    assertThat(setPropertyElements.get(1).getAttributeValue(PROPERTY_NAME_ATTR_NAME), is(CONTENT_TYPE));
    assertThat(setPropertyElements.get(1).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_APP_XML));
    assertThat(setPropertyElements.get(2).getAttributeValue(PROPERTY_NAME_ATTR_NAME), is(CONTENT_TYPE));
    assertThat(setPropertyElements.get(2).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_APP_JSON));
    List<Element> setPayloadElements = element.getChildren(SET_PAYLOAD_TAG_NAME, MULE_4_POLICY_NAMESPACE);
    assertThat(setPayloadElements, notNullValue());
    assertThat(setPayloadElements.size(), is(2));
    assertThat(setPayloadElements.get(0).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_SOAP_FAULT));
    assertThat(setPayloadElements.get(1).getAttributeValue(VALUE_ATTR_NAME), is(VALUE_ERROR_DESCRIPTION));
  }

  private void assertProxyElement(Element element) {
    Element root = getRootElement(element);
    Element proxyElement = root.getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(proxyElement, notNullValue());
    assertThat(proxyElement.getContentSize(), is(1));
    Element sourceElement = proxyElement.getChild(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(sourceElement, notNullValue());
    assertThat(sourceElement.getContentSize(), is(1));
    Element tryElement = sourceElement.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(tryElement, notNullValue());
    assertThat(tryElement.getContentSize(), is(2));
    Element executeNextElement = tryElement.getChild(EXECUTE_NEXT_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(executeNextElement, notNullValue());
    Element errorHandlerElement = tryElement.getChild(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(errorHandlerElement, notNullValue());
    Element onErrorContinueElement = errorHandlerElement.getChild(ON_ERROR_CONTINUE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(onErrorContinueElement, notNullValue());
    assertThat(onErrorContinueElement.getAttributeValue(TYPE_ATTR_NAME), is(CLIENT_ID_ENFORCEMENT_TYPE));
    assertThat(onErrorContinueElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertThat(onErrorContinueElement.getContentSize(), is(5));
    assertProcessorChain(onErrorContinueElement);
  }

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModel = amb.build();
  }

  @After
  public void cleanup() throws Exception {
    File dwFile = APPLICATION_MODEL_PATH.resolve(MIGRATION_RESOURCES_PATH).resolve("HttpListener.dwl").toFile();
    if (!dwFile.delete()) {
      dwFile.deleteOnExit();
    }
  }

  @Test
  public void assertValidateClient() {
    final ValidateClientTagMigrationStep step = new ValidateClientTagMigrationStep();
    Element element = getTestElement()
        .setAttribute(CLIENT_ID, CLIENT_ID_VALUE)
        .setAttribute(CLIENT_SECRET, CLIENT_SECRET_VALUE);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(VALIDATE_CLIENT_TAG_NAME));
    assertThat(element.getNamespace(), is(CLIENT_ID_ENFORCEMENT_NAMESPACE));
    assertThat(element.getAttributes().size(), is(3));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(CLIENT_ENFORCEMENT_CONFIG));
    assertThat(element.getAttribute(CLIENT_ID).getValue(), is(CLIENT_ID_VALUE));
    assertThat(element.getAttribute(CLIENT_SECRET).getValue(), is(CLIENT_SECRET_VALUE));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);
  }

  @Test
  public void assertValidateClientId() {
    final ValidateClientTagMigrationStep step = new ValidateClientTagMigrationStep();
    Element element = getTestElement()
        .setAttribute(CLIENT_ID, CLIENT_ID_VALUE);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(VALIDATE_CLIENT_ID_TAG_NAME));
    assertThat(element.getNamespace(), is(CLIENT_ID_ENFORCEMENT_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(CLIENT_ENFORCEMENT_CONFIG));
    assertThat(element.getAttribute(CLIENT_ID).getValue(), is(CLIENT_ID_VALUE));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);
  }

  @Test
  public void assertValidateBasicAuthEncodedClientNoProcessorChain() {
    final ValidateClientTagMigrationStep step = new ValidateClientTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(BASIC_AUTH_ENABLED, TRUE);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(VALIDATE_BASIC_AUTH_ENCODED_CLIENT_TAG_NAME));
    assertThat(element.getNamespace(), is(CLIENT_ID_ENFORCEMENT_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(CLIENT_ENFORCEMENT_CONFIG));
    assertThat(element.getAttribute(ENCODED_CLIENT).getValue(), is(ENCODED_CLIENT_VALUE));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);
  }

  @Test
  public void assertValidateBasicAuthEncodedClientWithProcessorChain() {
    final ValidateClientTagMigrationStep step = new ValidateClientTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(BASIC_AUTH_ENABLED, TRUE);

    addProcessorChainElement(element);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(VALIDATE_BASIC_AUTH_ENCODED_CLIENT_TAG_NAME));
    assertThat(element.getNamespace(), is(CLIENT_ID_ENFORCEMENT_NAMESPACE));
    assertThat(element.getAttributes().size(), is(2));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(CLIENT_ENFORCEMENT_CONFIG));
    assertThat(element.getAttribute(ENCODED_CLIENT).getValue(), is(ENCODED_CLIENT_VALUE));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);
    assertDwlScript();
    assertProxyElement(element);
  }

  @Test
  public void assertValidateBasicAuthFalseClientIdSecretWithProcessorChain() {
    final ValidateClientTagMigrationStep step = new ValidateClientTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(BASIC_AUTH_ENABLED, FALSE)
        .setAttribute(CLIENT_ID, CLIENT_ID_VALUE)
        .setAttribute(CLIENT_SECRET, CLIENT_SECRET_VALUE);

    addProcessorChainElement(element);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(VALIDATE_CLIENT_TAG_NAME));
    assertThat(element.getNamespace(), is(CLIENT_ID_ENFORCEMENT_NAMESPACE));
    assertThat(element.getAttributes().size(), is(3));
    assertThat(element.getAttribute(CONFIG_REF).getValue(), is(CLIENT_ENFORCEMENT_CONFIG));
    assertThat(element.getAttribute(CLIENT_ID).getValue(), is(CLIENT_ID_VALUE));
    assertThat(element.getAttribute(CLIENT_SECRET).getValue(), is(CLIENT_SECRET_VALUE));
    assertThat(element.getContent().size(), is(0));
    assertConfigElement(element);
    assertDwlScript();
    assertProxyElement(element);
  }

  @Test
  public void assertValidateBasicAuthFalseNoClientIdSecretWithProcessorChain() {
    final ValidateClientTagMigrationStep step = new ValidateClientTagMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .setAttribute(BASIC_AUTH_ENABLED, FALSE);

    addProcessorChainElement(element);

    step.execute(element, reportMock);

    assertThat(element.getName(), is(VALIDATE_CLIENT_TAG_NAME));
    assertThat(element.getNamespace(), is(API_PLATFORM_GW_MULE_3_NAMESPACE));
    verify(reportMock).report("clientIdEnforcement.invalidMigrationElement", element, element);
  }
}
