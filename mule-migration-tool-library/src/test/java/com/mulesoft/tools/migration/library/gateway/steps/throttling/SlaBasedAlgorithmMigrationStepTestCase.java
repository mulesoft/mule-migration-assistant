/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FALSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_THROTTLING_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_RESPONSE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.STATUS_CODE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.THROTTLING_EXTENSION_SLA_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRUE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_MULE_4_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.mulesoft.tools.migration.library.gateway.steps.policy.throttling.SlaBasedAlgorithmMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;

import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Test;

public class SlaBasedAlgorithmMigrationStepTestCase extends AbstractThrottlingTestCase {

  private static final String SLA_BASED_ALGORITHM_TAG_NAME = "sla-based-algorithm";

  private static final String VALIDATE_CLIENT_TAG_NAME = "validate-client";
  private static final String API_ID_ATTR_NAME = "apiId";
  private static final String API_ID_ATTR_VALUE = "${apiId}";

  private static final String ID_ATTR_VALUE_RATE_LIMIT_SLA = "{{policyId}}-rate-limit-sla";
  private static final String CLIENT_ID_ATTR_NAME = "clientId";
  private static final String CLIENT_ID_EXPRESSION_ATTR_NAME = "clientIdExpression";
  private static final String CLIENT_ID_ATTR_VALUE = "{{clientIdExpression}}";
  private static final String CLIENT_SECRET_ATTR_NAME = "clientSecret";
  private static final String CLIENT_SECRET_EXPRESSION_ATTR_NAME = "clientSecretExpression";
  private static final String CLIENT_SECRET_ATTR_VALUE = "{{clientSecretExpression}}";
  private static final String CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA = "rateLimitSlaConfig";

  private static final String TYPE_ATTR_VALUE_FORBIDDEN_CLIENT = "THROTTLING:FORBIDDEN_CLIENT";
  private static final String TYPE_ATTR_VALUE_UNKNOWN_API = "THROTTLING:UNKNOWN_API";
  private static final String STATUS_CODE_ATTR_VALUE_401 = "401";
  private static final String STATUS_CODE_ATTR_VALUE_503 = "503";
  private static final String DW_HEADERS_AUTHENTICATE_CLIENT_ID_ENFORCEMENT_RESPONSE_VALUE =
      "#[{'WWW-Authenticate': 'Client-ID-Enforcement'}]";

  @Override
  protected String getPolicyPom() {
    return "rate-limit-sla-pom.xml";
  }

  @Override
  protected void assertBeforeElements(Element beforeElement, int rateLimitElements, boolean isRateLimit) {
    assertThat(beforeElement, notNullValue());
    assertThat(beforeElement.getName(), is(RATE_LIMIT_TAG_NAME));
    assertThat(beforeElement.getNamespace(), is(THROTTLING_MULE_4_NAMESPACE));
    assertThat(beforeElement.getAttributes().size(), is(5));
    assertThat(beforeElement.getAttributeValue(ID), is(ID_ATTR_VALUE_RATE_LIMIT_SLA));
    assertThat(beforeElement.getAttributeValue(CLIENT_ID_ATTR_NAME), is(CLIENT_ID_ATTR_VALUE));
    assertThat(beforeElement.getAttributeValue(CLIENT_SECRET_ATTR_NAME), is(CLIENT_SECRET_ATTR_VALUE));
    assertThat(beforeElement.getAttributeValue(CONFIG_REF_ATTR_NAME), is(CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA));
    assertThat(beforeElement.getAttributeValue(TARGET_ATTR_NAME), is(TARGET_ATTR_VALUE));
  }

  private void assertForbiddenClientOnErrorContinueElement(Element forbiddenClientOnErrorContinueElement) {
    assertThat(forbiddenClientOnErrorContinueElement, notNullValue());
    assertThat(forbiddenClientOnErrorContinueElement.getName(), is(ON_ERROR_CONTINUE_TAG_NAME));
    assertThat(forbiddenClientOnErrorContinueElement.getNamespace(), is(MULE_4_CORE_NAMESPACE_NO_PREFIX));
    assertThat(forbiddenClientOnErrorContinueElement.getAttributes().size(), is(2));
    assertThat(forbiddenClientOnErrorContinueElement.getAttributeValue(TYPE_ATTR_NAME), is(TYPE_ATTR_VALUE_FORBIDDEN_CLIENT));
    assertThat(forbiddenClientOnErrorContinueElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertThat(forbiddenClientOnErrorContinueElement.getContentSize(), is(1));
    Element setResponseElement = forbiddenClientOnErrorContinueElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(setResponseElement, notNullValue());
    assertThat(setResponseElement.getAttributes().size(), is(1));
    assertThat(setResponseElement.getAttributeValue(STATUS_CODE), is(STATUS_CODE_ATTR_VALUE_401));
    assertThat(setResponseElement.getContentSize(), is(2));
    Element bodyElement = (Element) setResponseElement.getContent(0);
    assertThat(bodyElement, notNullValue());
    assertThat(bodyElement.getName(), is(BODY_TAG_NAME));
    assertThat(bodyElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    Text dwBodyContent = (Text) bodyElement.getContent(0);
    assertThat(dwBodyContent, notNullValue());
    assertThat(dwBodyContent.getText(), is(DW_BODY_RESEPONSE_VALUE));
    assertHeadersElement((Element) setResponseElement.getContent(1),
                         DW_HEADERS_AUTHENTICATE_CLIENT_ID_ENFORCEMENT_RESPONSE_VALUE);
  }

  private void assertUnknownApiOnErrorContinueElement(Element unknownApiOnErrorContinueElement) {
    assertThat(unknownApiOnErrorContinueElement, notNullValue());
    assertThat(unknownApiOnErrorContinueElement.getName(), is(ON_ERROR_CONTINUE_TAG_NAME));
    assertThat(unknownApiOnErrorContinueElement.getNamespace(), is(MULE_4_CORE_NAMESPACE_NO_PREFIX));
    assertThat(unknownApiOnErrorContinueElement.getAttributes().size(), is(2));
    assertThat(unknownApiOnErrorContinueElement.getAttributeValue(TYPE_ATTR_NAME), is(TYPE_ATTR_VALUE_UNKNOWN_API));
    assertThat(unknownApiOnErrorContinueElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertThat(unknownApiOnErrorContinueElement.getContentSize(), is(1));
    Element setResponseElement = unknownApiOnErrorContinueElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(setResponseElement, notNullValue());
    assertThat(setResponseElement.getAttributes().size(), is(1));
    assertThat(setResponseElement.getAttributeValue(STATUS_CODE), is(STATUS_CODE_ATTR_VALUE_503));
    assertThat(setResponseElement.getContentSize(), is(1));
    Element bodyElement = (Element) setResponseElement.getContent(0);
    assertThat(bodyElement, notNullValue());
    assertThat(bodyElement.getName(), is(BODY_TAG_NAME));
    assertThat(bodyElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    Text dwBodyContent = (Text) bodyElement.getContent(0);
    assertThat(dwBodyContent, notNullValue());
    assertThat(dwBodyContent.getText(), is(DW_BODY_RESEPONSE_VALUE));
  }

  @Override
  protected void assertErrorHandlerElement(Element errorHandlerElement, int rateLimitElements, boolean isRateLimit) {
    assertThat(errorHandlerElement, notNullValue());
    assertThat(errorHandlerElement.getName(), is(ERROR_HANDLER_TAG_NAME));
    assertThat(errorHandlerElement.getNamespace(), is(MULE_4_CORE_NAMESPACE_NO_PREFIX));
    assertThat(errorHandlerElement.getContentSize(), is(4));
    assertQuotaExceededOnErrorContinueElement((Element) errorHandlerElement.getContent(0), rateLimitElements, true);
    assertForbiddenClientOnErrorContinueElement((Element) errorHandlerElement.getContent(1));
    assertUnknownApiOnErrorContinueElement((Element) errorHandlerElement.getContent(2));
    assertOnErrorPropagateElement((Element) errorHandlerElement.getContent(3));
  }

  @Override
  protected void assertConfigContentElements(Element tierProviderElement, int rateLimitElements, boolean isRateLimit) {
    Element validateClientTagName = tierProviderElement.getChild(VALIDATE_CLIENT_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
    assertThat(validateClientTagName, notNullValue());
    assertThat(validateClientTagName.getAttributes().size(), is(1));
    assertThat(validateClientTagName.getAttributeValue(API_ID_ATTR_NAME), is(API_ID_ATTR_VALUE));
  }

  @Override
  protected void assertConfigAttr(Element config, int rateLimitElements, boolean isRateLimit) {
    assertThat(config.getAttributeValue(NAME_ATTR_NAME), is(CONFIG_NAME_ATTR_VALUE_RATE_LIMIT_SLA));
    assertThat(config.getAttributeValue(CLUSTERIZABLE_ATTR_NAME), is(TRUE));
  }

  private Element getSlaPolicy(int rateLimitElements, boolean isRateLimit) {
    return createPolicy(rateLimitElements, SLA_BASED_ALGORITHM_TAG_NAME, isRateLimit)
        .setAttribute(CLIENT_ID_EXPRESSION_ATTR_NAME, CLIENT_ID_ATTR_VALUE)
        .setAttribute(CLIENT_SECRET_EXPRESSION_ATTR_NAME, CLIENT_SECRET_ATTR_VALUE);
  }

  @Test
  public void oneRateLimitSLAElement() {
    SlaBasedAlgorithmMigrationStep step = new SlaBasedAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getSlaPolicy(1, true);

    step.execute(element, reportMock);

    assertConfigElement(element, 1, true);
    assertOperationElements(element, 1, true);
    assertNamespaces(element);
    assertPolicyName(element);
    verifyNoMoreInteractions(reportMock);
  }

  @Test
  public void multipleRateLimitSLAElements() {
    SlaBasedAlgorithmMigrationStep step = new SlaBasedAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getSlaPolicy(2, true);

    step.execute(element, reportMock);

    assertConfigElement(element, 2, true);
    assertOperationElements(element, 2, true);
    assertNamespaces(element);
    assertPolicyName(element);
    verifyNoMoreInteractions(reportMock);
  }

  @Test
  public void oneRateLimitSLAElementSpikeControl() {
    SlaBasedAlgorithmMigrationStep step = new SlaBasedAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getSlaPolicy(1, false);

    step.execute(element, reportMock);

    assertConfigElement(element, 1, false);
    assertOperationElements(element, 1, false);
    assertNamespaces(element);
    verify(reportMock).report("throttling.throttlingSLANotSupported", element.getParentElement(), element.getParentElement());
    assertPolicyName(element);
    verifyNoMoreInteractions(reportMock);
  }

  @Test
  public void multipleRateLimitSLAElementsSpikeControl() {
    SlaBasedAlgorithmMigrationStep step = new SlaBasedAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getSlaPolicy(2, false);

    step.execute(element, reportMock);

    assertConfigElement(element, 2, false);
    assertOperationElements(element, 2, false);
    assertNamespaces(element);
    verify(reportMock).report("throttling.throttlingSLANotSupported", element.getParentElement(), element.getParentElement());
    assertPolicyName(element);
    verifyNoMoreInteractions(reportMock);
  }

  @Test
  public void rateLimitSlaPomContributionTest() throws Exception {
    SlaBasedAlgorithmMigrationStep step = new SlaBasedAlgorithmMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getSlaPolicy(1, false);

    step.execute(element, reportMock);

    PomModel pm = appModel.getPomModel().get();

    assertThat(pm.getDependencies().size(), is(2));
    Dependency policyTransformExtensionDependency = pm.getDependencies().get(1);
    assertThat(policyTransformExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(policyTransformExtensionDependency.getArtifactId(), is(MULE_THROTTLING_EXTENSION_ARTIFACT_ID));
    assertThat(policyTransformExtensionDependency.getVersion(), is(THROTTLING_EXTENSION_SLA_VERSION));
    assertThat(policyTransformExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }
}
