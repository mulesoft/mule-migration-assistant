/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate client id enforcement error handler element
 *
 * @author Mulesoft Inc.
 */
public class ClientIdEnforcementOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String CLIENT_ID_ENFORCEMENT_TYPE =
      "CLIENT-ID-ENFORCEMENT:INVALID_API, CLIENT-ID-ENFORCEMENT:INVALID_CLIENT, CLIENT-ID-ENFORCEMENT:INVALID_CREDENTIALS";
  private static final String STATUS_CODE_VALUE = "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]";
  private static final String HEADERS_CONTENT_VALUE = "#[migration::HttpListener::httpListenerResponseHeaders(vars)]";

  @Override
  protected String getOnErrorContinueType() {
    return CLIENT_ID_ENFORCEMENT_TYPE;
  }

  @Override
  protected void setBodyElement(Element setResponseElement) {}

  @Override
  protected void setHeadersElement(Element setResponseElement) {
    setResponseElement
        .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(HEADERS_CONTENT_VALUE));
  }

  @Override
  protected String getStatusCodeValue() {
    return STATUS_CODE_VALUE;
  }
}
