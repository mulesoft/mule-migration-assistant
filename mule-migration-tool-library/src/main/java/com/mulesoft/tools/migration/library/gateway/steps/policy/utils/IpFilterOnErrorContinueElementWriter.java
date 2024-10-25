/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate ip filter error handler element
 *
 * @author Mulesoft Inc.
 */
public class IpFilterOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String IP_REJECTED = "IP:REJECTED";
  private static final String STATUS_CODE_VALUE = "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]";
  private static final String HEADERS_CONTENT_VALUE = "#[migration::HttpListener::httpListenerResponseHeaders(vars)]";

  @Override
  protected String getOnErrorContinueType() {
    return IP_REJECTED;
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
