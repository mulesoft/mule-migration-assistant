/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.http;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.HEADERS_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SET_RESPONSE_TAG_NAME;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Text;
import org.junit.Before;

public abstract class AbstractResponseBuilderMigrationStepTestCase {

  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/http/expected");

  protected static final String STATUS_ATTR_NAME = "status";
  protected static final String STATUS_ATTR_VALUE = "793";
  protected static final String CONTENT_TYPE_ATTR_NAME = "contentType";
  protected static final String CONTENT_TYPE_ATTR_VALUE = "application/json";

  private static final String SET_COOKIE_TAG_NAME = "set-cookie";
  private static final String COOKIE_NAME_ATTR_NAME = "name";
  private static final String COOKIE_NAME_ATTR_VALUE = "addedCookieName";
  private static final String VALUE_ATTR_NAME = "value";
  private static final String COOKIE_VALUE_ATTR_VALUE = "addedCookieValue";
  private static final String DOMAIN_ATTR_NAME = "domain";
  private static final String DOMAIN_ATTR_VALUE = "#[header:inbound:domain]";
  private static final String PATH_ATTR_NAME = "path";
  private static final String PATH_ATTR_VALUE = "#[header:inbound:path]";
  private static final String EXPIRY_DATE_ATTR_NAME = "expiryDate";
  private static final String EXPIRY_DATE_ATTR_VALUE = "#[header:inbound:expiryDate]";
  private static final String SECURE_ATTR_NAME = "secure";
  private static final String SECURE_ATTR_VALUE = "#[header:inbound:secure]";
  private static final String MAX_AGE_ATTR_NAME = "maxAge";
  private static final String MAX_AGE_ATTR_COOKIE_VALUE = "#[header:inbound:maxAge]";

  private static final String HEADERS_CDATA_VALUE = "#[{'testHeaderName' : 'testHeaderValue'}]";
  private static final String HEADER_NAME_ATTR_VALUE = "testHeaderName";
  private static final String HEADER_VALUE_ATTR_VALUE = "testHeaderValue";

  private static final String CACHE_CONTROL_TAG_NAME = "cache-control";
  private static final String MAX_AGE_ATTR_CACHE_CONTROL_NAME = "max-age";
  private static final String MAX_AGE_ATTR_CACHE_CONTROL_VALUE = "#[header:inbound:cacheControl]";

  private static final String EXPIRES_TAG_NAME = "expires";
  private static final String EXPIRES_VALUE_ATTR_VALUE = "Thu, 01 Dec 2014 16:00:00 GMT";

  private static final String LOCATION_TAG_NAME = "location";
  private static final String LOCATION_VALUE_ATTR_VALUE = "http://localhost:9090";

  protected static final String STATUS_CODE_ATTR_NAME = "statusCode";
  private static final String SET_COOKIE_PROPERTY_NAME = "Set-Cookie";
  private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";
  private static final String DOMAIN_PROPERTY_NAME = "Domain";
  private static final String PATH_PROPERTY_NAME = "Path";
  private static final String EXPIRES_PROPERTY_NAME = "Expires";
  private static final String SECURE_PROPERTY_NAME = "Secure";
  private static final String MAX_AGE_PROPERTY_NAME = "Max-Age";
  private static final String LOCATION_PROPERTY_NAME = "Location";
  private static final String CACHE_CONTROL_PROPERTY_NAME = "Cache-Control";
  protected static final String HEADERS_CONTENT_VALUE = "#[migration::HttpListener::httpListenerResponseHeaders(vars)";

  protected MigrationReport reportMock;
  protected ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    reportMock = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModel = amb.build();
  }

  protected abstract Element getTestElement();

  protected Element getHeadersElement() {
    return new Element(HEADERS_TAG_NAME, HTTP_NAMESPACE)
        .addContent(new CDATA(HEADERS_CDATA_VALUE));
  }

  protected Element getSimpleSetCookieElement() {
    return new Element(SET_COOKIE_TAG_NAME, HTTP_NAMESPACE).setAttribute(COOKIE_NAME_ATTR_NAME, COOKIE_NAME_ATTR_VALUE)
        .setAttribute(VALUE_ATTR_NAME, COOKIE_VALUE_ATTR_VALUE);
  }

  protected Element getFullSetCookieElement() {
    return new Element(SET_COOKIE_TAG_NAME, HTTP_NAMESPACE)
        .setAttribute(COOKIE_NAME_ATTR_NAME, COOKIE_NAME_ATTR_VALUE)
        .setAttribute(VALUE_ATTR_NAME, COOKIE_VALUE_ATTR_VALUE)
        .setAttribute(DOMAIN_ATTR_NAME, DOMAIN_ATTR_VALUE)
        .setAttribute(PATH_ATTR_NAME, PATH_ATTR_VALUE)
        .setAttribute(EXPIRY_DATE_ATTR_NAME, EXPIRY_DATE_ATTR_VALUE)
        .setAttribute(SECURE_ATTR_NAME, SECURE_ATTR_VALUE)
        .setAttribute(MAX_AGE_ATTR_NAME, MAX_AGE_ATTR_COOKIE_VALUE);
  }

  protected Element getCacheControlElement() {
    return new Element(CACHE_CONTROL_TAG_NAME, HTTP_NAMESPACE).setAttribute(MAX_AGE_ATTR_NAME,
                                                                            MAX_AGE_ATTR_CACHE_CONTROL_VALUE);
  }

  protected Element getExpiresElement() {
    return new Element(EXPIRES_TAG_NAME, HTTP_NAMESPACE).setAttribute(VALUE_ATTR_NAME, EXPIRES_VALUE_ATTR_VALUE);
  }

  protected Element getLocationElement() {
    return new Element(LOCATION_TAG_NAME, HTTP_NAMESPACE).setAttribute(VALUE_ATTR_NAME, LOCATION_VALUE_ATTR_VALUE);
  }

  protected Text assertBasicStructure(Element element) {
    assertThat(element.getName(), is(SET_RESPONSE_TAG_NAME));
    assertThat(element.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    assertThat(element.getAttributes().size(), is(1));
    assertThat(element.getContentSize(), is(1));
    Element headersElement = (Element) element.getContent().get(0);
    assertThat(headersElement.getName(), is(HEADERS_TAG_NAME));
    assertThat(headersElement.getNamespace(), is(HTTP_TRANSFORM_NAMESPACE));
    assertThat(headersElement.getContentSize(), is(1));
    return (Text) headersElement.getContent().get(0);
  }

  protected void assertSimpleContentTypeHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "'}]"));
  }

  protected void assertSimpleHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "'} ++ {'"
                   + HEADER_NAME_ATTR_VALUE + "' : '" + HEADER_VALUE_ATTR_VALUE + "'}]"));
  }

  protected void assertSimpleCookieHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "', '"
                   + SET_COOKIE_PROPERTY_NAME + "': '"
                   + COOKIE_NAME_ATTR_VALUE + "=" + COOKIE_VALUE_ATTR_VALUE + "'}]"));
  }

  protected void assertFullCookieHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "', '"
                   + SET_COOKIE_PROPERTY_NAME + "': '" + COOKIE_NAME_ATTR_VALUE + "=" + COOKIE_VALUE_ATTR_VALUE + "; "
                   + DOMAIN_PROPERTY_NAME + "=" + DOMAIN_ATTR_VALUE + "; "
                   + PATH_PROPERTY_NAME + "=" + PATH_ATTR_VALUE + "; "
                   + EXPIRES_PROPERTY_NAME + "=" + EXPIRY_DATE_ATTR_VALUE + "; "
                   + SECURE_PROPERTY_NAME + "; "
                   + MAX_AGE_PROPERTY_NAME + "=" + MAX_AGE_ATTR_COOKIE_VALUE
                   + "'}]"));
  }

  protected void assertCacheControlHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "', '"
                   + CACHE_CONTROL_PROPERTY_NAME + "': '" + MAX_AGE_ATTR_CACHE_CONTROL_NAME + "="
                   + MAX_AGE_ATTR_CACHE_CONTROL_VALUE + "'}]"));
  }

  protected void assertExpiresHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "', '"
                   + EXPIRES_PROPERTY_NAME + "': '" + EXPIRES_VALUE_ATTR_VALUE + "'}]"));
  }

  protected void assertLocationHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "', '"
                   + LOCATION_PROPERTY_NAME + "': '" + LOCATION_VALUE_ATTR_VALUE + "'}]"));
  }

  protected void assertNoStatusHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "'}]"));
  }

  protected void assertFullResponseHeaders(String headersText) {
    assertThat(headersText,
               is(HEADERS_CONTENT_VALUE + " ++ {'" + CONTENT_TYPE_PROPERTY_NAME + "': '" + CONTENT_TYPE_ATTR_VALUE + "', '"
                   + SET_COOKIE_PROPERTY_NAME + "': '" + COOKIE_NAME_ATTR_VALUE + "=" + COOKIE_VALUE_ATTR_VALUE + "; "
                   + DOMAIN_PROPERTY_NAME + "=" + DOMAIN_ATTR_VALUE + "; "
                   + PATH_PROPERTY_NAME + "=" + PATH_ATTR_VALUE + "; "
                   + EXPIRES_PROPERTY_NAME + "=" + EXPIRY_DATE_ATTR_VALUE + "; "
                   + SECURE_PROPERTY_NAME + "; "
                   + MAX_AGE_PROPERTY_NAME + "=" + MAX_AGE_ATTR_COOKIE_VALUE + "', '"
                   + CACHE_CONTROL_PROPERTY_NAME + "': '" + MAX_AGE_ATTR_CACHE_CONTROL_NAME + "="
                   + MAX_AGE_ATTR_CACHE_CONTROL_VALUE + "', '"
                   + EXPIRES_PROPERTY_NAME + "': '" + EXPIRES_VALUE_ATTR_VALUE + "', '"
                   + LOCATION_PROPERTY_NAME + "': '" + LOCATION_VALUE_ATTR_VALUE
                   + "'} ++ {'"
                   + HEADER_NAME_ATTR_VALUE + "' : '" + HEADER_VALUE_ATTR_VALUE + "'}]"));
  }

}
