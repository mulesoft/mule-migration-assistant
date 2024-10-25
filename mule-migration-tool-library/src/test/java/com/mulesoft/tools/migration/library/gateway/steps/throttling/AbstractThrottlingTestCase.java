/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.CONFIG;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FALSE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PROXY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SCHEMA_LOCATION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_RESPONSE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SOURCE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.STATUS_CODE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TRY_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_MULE_4_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XSI_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.junit.Before;

public abstract class AbstractThrottlingTestCase {

  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/throttling/expected");
  private static final Namespace HTTP_POLICY_NAMESPACE =
      Namespace.getNamespace("http-policy", "http://www.mulesoft.org/schema/mule/http-policy");

  protected static final String RATE_LIMIT_TAG_NAME = "rate-limit";
  protected static final String MAXIMUM_REQUESTS_PER_PERIOD_ATTR_NAME = "maximumRequestsPerPeriod";
  protected static final String TIME_PERIOD_MILLIS_ATTR_NAME = "timePeriodMillis";
  protected static final String TIME_PERIOD_MILLIS_ATTR_VALUE = "{{timePeriodInMilliseconds}}";

  private static final String DISCARD_RESPONSE_TAG_NAME = "discard-response";

  private static final String DELAY_RESPONSE_TAG_NAME = "delay-response";
  private static final String DELAY_TIME_IN_MILLIS_ATTR_NAME = "delayTimeInMillis";
  protected static final String DELAY_TIME_IN_MILLIS_ATTR_VALUE = "{{delayTimeInMillis}}";
  private static final String DELAY_ATTEMPTS_ATTR_NAME = "delayAttempts";
  protected static final String DELAY_ATTEMPTS_ATTR_VALUE = "{{delayAttempts}}";

  protected static final String NAME_ATTR_NAME = "name";
  protected static final String CLUSTERIZABLE_ATTR_NAME = "clusterizable";
  private static final String TIER_PROVIDER_TAG_NAME = "tier-provider";

  private static final String EXECUTE_NEXT_TAG_NAME = "execute-next";
  protected static final String ERROR_HANDLER_TAG_NAME = "error-handler";
  protected static final String ON_ERROR_PROPAGATE_TAG_NAME = "on-error-propagate";

  protected static final String CONFIG_REF_ATTR_NAME = "config-ref";
  protected static final String TARGET_ATTR_NAME = "target";
  protected static final String TARGET_ATTR_VALUE = "throttlingResponse";

  private static final String ADD_HEADERS_TAG_NAME = "add-headers";
  private static final String OUTPUT_TYPE_ATTR_NAME = "outputType";
  private static final String OUTPUT_TYPE_ATTR_VALUE = "response";

  private static final String HEADERS_TAG_NAME = "headers";
  private static final String DW_HEADERS_RESPONSE_VALUE =
      "#[ { 'x-ratelimit-remaining': vars.throttlingResponse.availableQuota as String, 'x-ratelimit-limit': vars.throttlingResponse.maximumAllowedRequests as String, 'x-ratelimit-reset': vars.throttlingResponse.remainingFrame as String } ]";
  protected static final String DW_HEADERS_EXCEPTION_RESPONSE_VALUE =
      "#[ { 'x-ratelimit-remaining': error.exception.availableQuota as String, 'x-ratelimit-limit': error.exception.maximumAllowedRequests as String, 'x-ratelimit-reset': error.exception.remainingFrame as String } ]";

  protected static final String ON_ERROR_CONTINUE_TAG_NAME = "on-error-continue";
  protected static final String TYPE_ATTR_NAME = "type";
  private static final String TYPE_ATTR_VALUE_QUOTA_EXCEEDED = "THROTTLING:QUOTA_EXCEEDED";
  private static final String TYPE_ATTR_VALUE_THROTTLING = TYPE_ATTR_VALUE_QUOTA_EXCEEDED + ", THROTTLING:QUEUING_LIMIT_REACHED";
  protected static final String LOG_EXCEPTION_ATTR_NAME = "logException";

  private static final String STATUS_CODE_ATTR_VALUE_429 = "429";

  protected static final String BODY_TAG_NAME = "body";
  protected static final String DW_BODY_RESEPONSE_VALUE =
      "#[ output application/json --- {\"error\": \"$(error.description)\"} ]";

  private static final String THROTTLING_NAMESPACE_PREFIX = "throttling";
  private static final String HTTP_TRANSFORM_NAMESPACE_PREFIX = "http-transform";
  private static final String THROTTLING_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/throttling http://www.mulesoft.org/schema/mule/throttling/current/mule-throttling.xsd";
  private static final String HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/http-policy-transform http://www.mulesoft.org/schema/mule/http-policy-transform/current/mule-http-policy-transform.xsd";

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder appModelBuilder = new ApplicationModel.ApplicationModelBuilder();
    appModelBuilder.withProjectType(ProjectType.MULE_THREE_POLICY);
    appModelBuilder.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModelBuilder.withPom(APPLICATION_MODEL_PATH.resolve(getPolicyPom()));
    appModel = appModelBuilder.build();
  }

  protected abstract String getPolicyPom();

  private Element getRateLimitElement(String maximumRequestsValue) {
    return new Element(RATE_LIMIT_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE)
        .setAttribute(MAXIMUM_REQUESTS_PER_PERIOD_ATTR_NAME, maximumRequestsValue)
        .setAttribute(TIME_PERIOD_MILLIS_ATTR_NAME, TIME_PERIOD_MILLIS_ATTR_VALUE);
  }

  private Element getDelayResponseElement() {
    return new Element(DELAY_RESPONSE_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE)
        .setAttribute(DELAY_TIME_IN_MILLIS_ATTR_NAME, DELAY_TIME_IN_MILLIS_ATTR_VALUE)
        .setAttribute(DELAY_ATTEMPTS_ATTR_NAME, DELAY_ATTEMPTS_ATTR_VALUE);
  }

  private Element getNewElement(String elementTagName) {
    return new Element(elementTagName, THROTTLING_GW_MULE_3_NAMESPACE);
  }

  protected Element createPolicy(int rateLimitElements, String algorithmTagName, boolean isRateLimit) {
    Element policy = new Element(POLICY_TAG_NAME, MULE_3_POLICY_NAMESPACE);
    Element throttlingPolicy = new Element(POLICY_TAG_NAME, THROTTLING_GW_MULE_3_NAMESPACE);
    throttlingPolicy.addContent(isRateLimit ? getNewElement(DISCARD_RESPONSE_TAG_NAME) : getDelayResponseElement());
    Element algorithmElement = getNewElement(algorithmTagName);
    IntStream.range(0, rateLimitElements).mapToObj(i -> getRateLimitElement(String.valueOf(i)))
        .forEach(algorithmElement::addContent);
    throttlingPolicy.addContent(algorithmElement);
    new Document().setRootElement(policy.addContent(throttlingPolicy));
    return algorithmElement;
  }

  protected void assertConfigElement(Element element, int rateLimitElements, boolean isRateLimit) {
    Element rootElement = element.getDocument().getRootElement();
    Element configElement = rootElement.getChild(CONFIG, THROTTLING_MULE_4_NAMESPACE);
    assertThat(configElement, notNullValue());
    assertConfigAttr(configElement, rateLimitElements, isRateLimit);
    Element tierProviderElement = configElement.getChild(TIER_PROVIDER_TAG_NAME, THROTTLING_MULE_4_NAMESPACE);
    assertThat(tierProviderElement, notNullValue());
    assertConfigContentElements(tierProviderElement, rateLimitElements, isRateLimit);
  }

  protected Element assertBasicStructureElements(Element element) {
    Element rootElement = element.getDocument().getRootElement();
    Element proxyElement = rootElement.getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(proxyElement, notNullValue());
    assertThat(proxyElement.getContentSize(), is(1));
    Element sourceElement = proxyElement.getChild(SOURCE_TAG_NAME, HTTP_POLICY_NAMESPACE);
    assertThat(sourceElement, notNullValue());
    assertThat(sourceElement.getContentSize(), is(1));
    Element tryElement = sourceElement.getChild(TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    assertThat(tryElement, notNullValue());
    assertThat(tryElement.getContentSize(), is(4));
    return tryElement;
  }

  protected void assertHeadersElement(Element headersElement, String expectedDWResponseValue) {
    assertThat(headersElement, notNullValue());
    assertThat(headersElement.getName(), is(HEADERS_TAG_NAME));
    assertThat(headersElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    assertThat(headersElement.getContentSize(), is(1));
    Text dwHeadersContent = (Text) headersElement.getContent(0);
    assertThat(dwHeadersContent, notNullValue());
    assertThat(dwHeadersContent.getText(), is(expectedDWResponseValue));
  }

  protected void assertAddHeadersElement(Element addHeadersElement) {
    assertThat(addHeadersElement, notNullValue());
    assertThat(addHeadersElement.getName(), is(ADD_HEADERS_TAG_NAME));
    assertThat(addHeadersElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    assertThat(addHeadersElement.getAttributes().size(), is(1));
    assertThat(addHeadersElement.getAttributeValue(OUTPUT_TYPE_ATTR_NAME), is(OUTPUT_TYPE_ATTR_VALUE));
    assertThat(addHeadersElement.getContentSize(), is(1));
    assertHeadersElement((Element) addHeadersElement.getContent(0), DW_HEADERS_RESPONSE_VALUE);
  }

  protected void assertOnErrorPropagateElement(Element onErrorPropagate) {
    assertThat(onErrorPropagate, notNullValue());
    assertThat(onErrorPropagate.getName(), is(ON_ERROR_PROPAGATE_TAG_NAME));
    assertThat(onErrorPropagate.getNamespace(), is(MULE_4_CORE_NAMESPACE_NO_PREFIX));
    assertThat(onErrorPropagate.getContentSize(), is(1));
    assertAddHeadersElement((Element) onErrorPropagate.getContent(0));
  }

  protected void assertQuotaExceededOnErrorContinueElement(Element onErrorContinueElement, int rateLimitElements,
                                                           boolean isRateLimit) {
    assertThat(onErrorContinueElement, notNullValue());
    assertThat(onErrorContinueElement.getName(), is(ON_ERROR_CONTINUE_TAG_NAME));
    assertThat(onErrorContinueElement.getNamespace(), is(MULE_4_CORE_NAMESPACE_NO_PREFIX));
    assertThat(onErrorContinueElement.getAttributes().size(), is(2));
    assertThat(onErrorContinueElement.getAttributeValue(TYPE_ATTR_NAME),
               is(isRateLimit || rateLimitElements > 1 ? TYPE_ATTR_VALUE_QUOTA_EXCEEDED : TYPE_ATTR_VALUE_THROTTLING));
    assertThat(onErrorContinueElement.getAttributeValue(LOG_EXCEPTION_ATTR_NAME), is(FALSE));
    assertThat(onErrorContinueElement.getContentSize(), is(1));
    Element setResponseElement = onErrorContinueElement.getChild(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    assertThat(setResponseElement, notNullValue());
    assertThat(setResponseElement.getAttributes().size(), is(1));
    assertThat(setResponseElement.getAttributeValue(STATUS_CODE), is(STATUS_CODE_ATTR_VALUE_429));
    assertThat(setResponseElement.getContentSize(), is(2));
    Element bodyElement = (Element) setResponseElement.getContent(0);
    assertThat(bodyElement, notNullValue());
    assertThat(bodyElement.getName(), is(BODY_TAG_NAME));
    assertThat(bodyElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    Text dwBodyContent = (Text) bodyElement.getContent(0);
    assertThat(dwBodyContent, notNullValue());
    assertThat(dwBodyContent.getText(), is(DW_BODY_RESEPONSE_VALUE));
    assertHeadersElement((Element) setResponseElement.getContent(1), DW_HEADERS_EXCEPTION_RESPONSE_VALUE);
  }

  protected void assertOperationElements(Element element, int rateLimitElements, boolean isRateLimit) {
    Element tryElement = assertBasicStructureElements(element);
    assertBeforeElements((Element) tryElement.getContent(0), rateLimitElements, isRateLimit);
    Element executeNextElement = (Element) tryElement.getContent(1);
    assertThat(executeNextElement, notNullValue());
    assertThat(executeNextElement.getName(), is(EXECUTE_NEXT_TAG_NAME));
    assertThat(executeNextElement.getNamespace(), is(HTTP_POLICY_NAMESPACE));
    assertAddHeadersElement((Element) tryElement.getContent(2));
    assertErrorHandlerElement((Element) tryElement.getContent(3), rateLimitElements, isRateLimit);
  }

  protected void assertNamespaces(Element element) {
    Element rootElement = element.getDocument().getRootElement();
    assertThat(rootElement.getNamespace(THROTTLING_NAMESPACE_PREFIX), notNullValue());
    assertThat(rootElement.getNamespace(THROTTLING_NAMESPACE_PREFIX), is(THROTTLING_MULE_4_NAMESPACE));
    assertThat(rootElement.getNamespace(HTTP_TRANSFORM_NAMESPACE_PREFIX), notNullValue());
    assertThat(rootElement.getNamespace(HTTP_TRANSFORM_NAMESPACE_PREFIX), is(HTTP_TRANSFORM_NAMESPACE));
    String schemaLocation = rootElement.getAttributeValue(SCHEMA_LOCATION, XSI_NAMESPACE);
    assert (schemaLocation.contains(THROTTLING_XSI_SCHEMA_LOCATION_URI));
    assert (schemaLocation.contains(HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI));
  }

  private Element getProxyElement(Element fixedTimeElement) {
    return fixedTimeElement.getDocument().getRootElement().getChild(PROXY_TAG_NAME, HTTP_POLICY_NAMESPACE);
  }

  protected void assertPolicyName(Element element) {
    Element proxyElement = getProxyElement(element);
    verify(reportMock).report("basicStructure.defaultPolicyName", proxyElement, proxyElement,
                              appModel.getPomModel().get().getArtifactId());
  }

  protected abstract void assertConfigAttr(Element config, int rateLimitElements, boolean isRateLimit);

  protected abstract void assertConfigContentElements(Element element, int rateLimitElements, boolean isRateLimit);

  protected abstract void assertBeforeElements(Element beforeElement, int rateLimitElements, boolean isRateLimit);

  protected abstract void assertErrorHandlerElement(Element errorHandlerElement, int rateLimitElements, boolean isRateLimit);

}
