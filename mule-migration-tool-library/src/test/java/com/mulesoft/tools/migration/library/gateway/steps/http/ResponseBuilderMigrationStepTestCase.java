/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.http;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.mulesoft.tools.migration.library.gateway.steps.policy.http.ResponseBuilderMigrationStep;

import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Test;

public class ResponseBuilderMigrationStepTestCase extends AbstractResponseBuilderMigrationStepTestCase {

  private static final String RESPONSE_BUILDER_TAG_NAME = "response-builder";
  private static final String HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE =
      "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]";

  @Override
  protected Element getTestElement() {
    return new Element(RESPONSE_BUILDER_TAG_NAME, HTTP_NAMESPACE).setAttribute(STATUS_ATTR_NAME, STATUS_ATTR_VALUE)
        .setAttribute(CONTENT_TYPE_ATTR_NAME, CONTENT_TYPE_ATTR_VALUE);
  }

  @Test
  public void simpleResponseBuilderElement() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertSimpleContentTypeHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderElementWithHeader() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getHeadersElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertSimpleHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderElementWithCookie() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getSimpleSetCookieElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertSimpleCookieHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderElementWithCookieFull() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getFullSetCookieElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertFullCookieHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderElementWithCacheControl() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getCacheControlElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertCacheControlHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderElementWithExpires() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getExpiresElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertExpiresHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderElementWithLocation() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getLocationElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertLocationHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderWithNoStatus() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();
    element.removeAttribute(STATUS_ATTR_NAME);

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE));
    assertNoStatusHeaders(headersText.getText());
  }

  @Test
  public void responseBuilderWithNoContentType() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement();
    element.removeAttribute(CONTENT_TYPE_ATTR_NAME);

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertThat(headersText.getText(), is(HEADERS_CONTENT_VALUE + "]"));
  }

  @Test
  public void responseBuilderRaw() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = new Element(RESPONSE_BUILDER_TAG_NAME, HTTP_NAMESPACE);

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE));
    assertThat(headersText.getText(), is(HEADERS_CONTENT_VALUE + "]"));
  }

  @Test
  public void responseBuilderFull() {
    ResponseBuilderMigrationStep step = new ResponseBuilderMigrationStep();
    step.setApplicationModel(appModel);
    Element element = getTestElement()
        .addContent(getHeadersElement())
        .addContent(getFullSetCookieElement())
        .addContent(getCacheControlElement())
        .addContent(getExpiresElement())
        .addContent(getLocationElement());

    step.execute(element, reportMock);

    Text headersText = assertBasicStructure(element);
    assertThat(element.getAttributeValue(STATUS_CODE_ATTR_NAME), is(STATUS_ATTR_VALUE));
    assertFullResponseHeaders(headersText.getText());
  }

}
