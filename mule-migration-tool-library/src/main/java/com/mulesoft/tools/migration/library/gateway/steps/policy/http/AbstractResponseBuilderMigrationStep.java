/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.http;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.library.gateway.steps.policy.mule.HttpTransformPomContributionMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;

/**
 * Common stuff to migrate http listener
 *
 * @author Mulesoft Inc.
 */
public abstract class AbstractResponseBuilderMigrationStep extends PolicyMigrationStep {

  private final Pattern EXPRESSION_WRAPPER = Pattern.compile("^\\s*#\\[(.*)]\\s*$", Pattern.DOTALL);

  private static final String SET_COOKIE_TAG_NAME = "set-cookie";
  private static final String CACHE_CONTROL_TAG_NAME = "cache-control";
  private static final String EXPIRES_TAG_NAME = "expires";
  private static final String LOCATION_TAG_NAME = "location";

  private static final String STATUS_ATTR_NAME = "status";
  private static final String CONTENT_TYPE_ATTR_NAME = "contentType";
  private static final String VALUE_ATTR_NAME = "value";

  private static final String SET_RESPONSE_TAG_NAME = "set-response";
  private static final String HEADERS_TAG_NAME = "headers";

  private static final String STATUS_CODE_ATTR_NAME = "statusCode";
  private static final String SET_COOKIE_PROPERTY_NAME = "Set-Cookie";
  private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";
  private static final String EXPIRES_PROPERTY_NAME = "Expires";
  private static final String LOCATION_PROPERTY_NAME = "Location";
  private static final String CACHE_CONTROL_PROPERTY_NAME = "Cache-Control";

  private static final String HEADERS_CONTENT_VALUE = "#[migration::HttpListener::httpListenerResponseHeaders(vars)";
  private static final String HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/http-policy-transform http://www.mulesoft.org/schema/mule/http-policy-transform/current/mule-http-policy-transform.xsd";

  public AbstractResponseBuilderMigrationStep(final Namespace namespace, final String tagName) {
    super(namespace, tagName);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.setName(SET_RESPONSE_TAG_NAME);
    element.setNamespace(HTTP_TRANSFORM_NAMESPACE);
    addNamespaceDeclaration(getRootElement(element), HTTP_TRANSFORM_NAMESPACE, HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI);
    setStatusCode(element);
    Element headersElement = new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    Text textElement = new Text(HEADERS_CONTENT_VALUE + getHeadersTextContentMap(element) + "]");
    headersElement
        .addContent(textElement);
    element.addContent(headersElement);
    com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.httpListenerLib(getApplicationModel());
    new HttpTransformPomContributionMigrationStep().execute(getApplicationModel().getPomModel().get(), migrationReport);
  }

  private void setStatusCode(Element setResponseElement) {
    if (setResponseElement.getAttribute(STATUS_ATTR_NAME) != null) {
      setResponseElement.setAttribute(STATUS_CODE_ATTR_NAME, setResponseElement.getAttributeValue(STATUS_ATTR_NAME));
      setResponseElement.removeAttribute(STATUS_ATTR_NAME);
    } else if (setResponseElement.getAttribute(STATUS_CODE_ATTR_NAME) == null) {
      setResponseElement.setAttribute(STATUS_CODE_ATTR_NAME, getListenerResponseStatusCode());
    }
  }

  private String getHeadersTextContentMap(Element setResponseElement) {
    String headers = StringUtils.removeEnd(new StringBuilder().append(extractContentType(setResponseElement))
        .append(extractCookies(setResponseElement))
        .append(extractCacheControl(setResponseElement)).append(extractExpires(setResponseElement))
        .append(extractLocation(setResponseElement)).toString(), ", ");
    return headers.length() > 0 ? " ++ {" + headers + "}" + extractHeaders(setResponseElement) : "";
  }

  private String extractContentType(Element setResponseElement) {
    StringBuilder contentTypeBuilder = new StringBuilder();
    if (setResponseElement.getAttribute(CONTENT_TYPE_ATTR_NAME) != null) {
      contentTypeBuilder.append("'").append(CONTENT_TYPE_PROPERTY_NAME).append("': '")
          .append(setResponseElement.getAttributeValue(CONTENT_TYPE_ATTR_NAME)).append("', ");
      setResponseElement.removeAttribute(CONTENT_TYPE_ATTR_NAME);
    }
    return contentTypeBuilder.toString();
  }


  private String extractCookies(Element setResponseElement) {
    StringBuilder cookies = new StringBuilder();
    new ArrayList<>(setResponseElement.getChildren(SET_COOKIE_TAG_NAME, HTTP_NAMESPACE)).forEach(setCookie -> {
      StringBuilder cookieBuilder = new StringBuilder().append("'").append(SET_COOKIE_PROPERTY_NAME).append("': '");
      cookieBuilder.append(setCookie.getAttributeValue("name")).append("=").append(setCookie.getAttributeValue("value"))
          .append("; ");
      if (setCookie.getAttribute("domain") != null) {
        cookieBuilder.append("Domain=").append(setCookie.getAttributeValue("domain")).append("; ");
      }
      if (setCookie.getAttribute("path") != null) {
        cookieBuilder.append("Path=").append(setCookie.getAttributeValue("path")).append("; ");
      }
      if (setCookie.getAttribute("expiryDate") != null) {
        cookieBuilder.append("Expires=").append(setCookie.getAttributeValue("expiryDate")).append("; ");
      }
      if (setCookie.getAttribute("secure") != null) {
        cookieBuilder.append("Secure; ");
      }
      if (setCookie.getAttribute("maxAge") != null) {
        cookieBuilder.append("Max-Age=").append(setCookie.getAttributeValue("maxAge")).append("; ");
      }
      cookies.append(StringUtils.removeEnd(cookieBuilder.toString(), "; ")).append("', ");
      setCookie.detach();
    });
    return cookies.toString();
  }

  private String unwrap(String headersContent) {
    Matcher wrappedExpressionMatcher = EXPRESSION_WRAPPER.matcher(headersContent);
    if (wrappedExpressionMatcher.matches()) {
      return unwrap(wrappedExpressionMatcher.group(1).trim());
    }
    return headersContent;
  }

  private String extractHeaders(Element setResponseElement) {
    StringBuilder headers = new StringBuilder();
    if (!setResponseElement.getChildren(HEADERS_TAG_NAME, HTTP_NAMESPACE).isEmpty()) {
      Element headersElement = setResponseElement.getChild(HEADERS_TAG_NAME, HTTP_NAMESPACE);
      headersElement.getContent().stream().filter(content -> content instanceof CDATA)
          .forEach(content -> headers.append(" ++ ").append(unwrap(((CDATA) content).getText())));
      headersElement.detach();
    }
    return headers.toString();
  }

  private String extractCacheControl(Element setResponseElement) {
    StringBuilder cacheControlBuilder = new StringBuilder();
    if (setResponseElement.getChild(CACHE_CONTROL_TAG_NAME, HTTP_NAMESPACE) != null) {
      Element cacheControlElement = setResponseElement.getChild(CACHE_CONTROL_TAG_NAME, HTTP_NAMESPACE);
      if (cacheControlElement.getAttribute("directive") != null) {
        cacheControlBuilder.append("'").append(CACHE_CONTROL_PROPERTY_NAME).append("': '")
            .append(cacheControlElement.getAttributeValue("directive")).append("', ");
      }
      if (cacheControlElement.getAttribute("noCache") != null
          && "true".equals(cacheControlElement.getAttributeValue("noCache"))) {
        cacheControlBuilder.append("'").append(CACHE_CONTROL_PROPERTY_NAME).append("': 'no-cache', ");
      }
      if (cacheControlElement.getAttribute("noStore") != null
          && "true".equals(cacheControlElement.getAttributeValue("noStore"))) {
        cacheControlBuilder.append("'").append(CACHE_CONTROL_PROPERTY_NAME).append("': 'no-store', ");
      }
      if (cacheControlElement.getAttribute("mustRevalidate") != null
          && "true".equals(cacheControlElement.getAttributeValue("mustRevalidate"))) {
        cacheControlBuilder.append("'").append(CACHE_CONTROL_PROPERTY_NAME).append("': 'must-revalidate', ");
      }
      if (cacheControlElement.getAttribute("maxAge") != null) {
        cacheControlBuilder.append("'").append(CACHE_CONTROL_PROPERTY_NAME).append("': 'max-age=")
            .append(cacheControlElement.getAttributeValue("maxAge")).append("', ");
      }
      cacheControlElement.detach();
    }
    return cacheControlBuilder.toString();
  }

  private String extractExpires(Element setResponseElement) {
    StringBuilder expiresBuilder = new StringBuilder();
    if (setResponseElement.getChild(EXPIRES_TAG_NAME, HTTP_NAMESPACE) != null) {
      Element expiresElement = setResponseElement.getChild(EXPIRES_TAG_NAME, HTTP_NAMESPACE);
      expiresBuilder.append("'").append(EXPIRES_PROPERTY_NAME).append("': '")
          .append(expiresElement.getAttributeValue(VALUE_ATTR_NAME)).append("', ");
      expiresElement.detach();
    }
    return expiresBuilder.toString();
  }

  private String extractLocation(Element setResponseElement) {
    StringBuilder locationBuilder = new StringBuilder();
    if (setResponseElement.getChild(LOCATION_TAG_NAME, HTTP_NAMESPACE) != null) {
      Element locationElement = setResponseElement.getChild(LOCATION_TAG_NAME, HTTP_NAMESPACE);
      locationBuilder.append("'").append(LOCATION_PROPERTY_NAME).append("': '")
          .append(locationElement.getAttributeValue(VALUE_ATTR_NAME)).append("', ");
      locationElement.detach();
    }
    return locationBuilder.toString();
  }

  protected abstract String getListenerResponseStatusCode();

}
