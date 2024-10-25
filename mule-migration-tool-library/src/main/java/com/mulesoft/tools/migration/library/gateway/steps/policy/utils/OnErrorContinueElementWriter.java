/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;

import org.jdom2.Element;

/**
 * Migrate error handler element
 *
 * @author Mulesoft Inc.
 */
public abstract class OnErrorContinueElementWriter {

  protected static final String ON_ERROR_CONTINUE_TAG_NAME = "on-error-continue";
  protected static final String TYPE_ATTR_NAME = "type";
  private static final String LOG_EXCEPTION_ATTR_NAME = "logException";
  private static final String FALSE_VALUE = "false";

  private static final String SET_RESPONSE_TAG_NAME = "set-response";
  private static final String STATUS_CODE_ATTR_NAME = "statusCode";

  protected static final String BODY_TAG_NAME = "body";
  protected static final String DW_BODY_RESEPONSE_VALUE =
      "#[ output application/json --- {\"error\": \"$(error.description)\"} ]";

  protected static final String HEADERS_TAG_NAME = "headers";
  protected static final String DW_HEADERS_EXCEPTION_RESPONSE_VALUE =
      "#[ { 'x-ratelimit-remaining': error.exception.availableQuota as String, 'x-ratelimit-limit': error.exception.maximumAllowedRequests as String, 'x-ratelimit-reset': error.exception.remainingFrame as String } ]";

  protected static final String STATUS_CODE_503 = "503";
  protected static final String STATUS_CODE_500 = "500";
  protected static final String STATUS_CODE_429 = "429";
  protected static final String STATUS_CODE_403 = "403";
  protected static final String STATUS_CODE_401 = "401";
  protected static final String STATUS_CODE_400 = "400";

  protected Element getOnErrorContinueElement(Element errorHandlerElement) {
    return errorHandlerElement.getChildren(ON_ERROR_CONTINUE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX).stream()
        .filter(element -> element.getAttributeValue(TYPE_ATTR_NAME).equals(getOnErrorContinueType())).findFirst().orElse(null);
  }

  protected abstract String getOnErrorContinueType();

  protected abstract void setBodyElement(Element setResponseElement);

  protected abstract void setHeadersElement(Element setResponseElement);

  protected abstract String getStatusCodeValue();

  private Element getSetResponseElement() {
    Element setResponseElement = new Element(SET_RESPONSE_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
        .setAttribute(STATUS_CODE_ATTR_NAME, getStatusCodeValue());
    setBodyElement(setResponseElement);
    setHeadersElement(setResponseElement);
    return setResponseElement;
  }

  public Element create(Element errorHandlerElement, boolean addSetResponseElement) {
    Element onErrorContinueElement = getOnErrorContinueElement(errorHandlerElement);
    if (onErrorContinueElement == null) {
      onErrorContinueElement = new Element(ON_ERROR_CONTINUE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
          .setAttribute(TYPE_ATTR_NAME, getOnErrorContinueType())
          .setAttribute(LOG_EXCEPTION_ATTR_NAME, FALSE_VALUE);
      if (addSetResponseElement) {
        onErrorContinueElement.addContent(getSetResponseElement());
      }
      errorHandlerElement.addContent(onErrorContinueElement);
    }
    return onErrorContinueElement;
  }
}
