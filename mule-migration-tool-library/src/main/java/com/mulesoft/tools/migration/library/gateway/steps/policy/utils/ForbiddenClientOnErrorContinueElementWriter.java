/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate forbidden client error handler element
 *
 * @author Mulesoft Inc.
 */
public class ForbiddenClientOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String THROTTLING_FORBIDDEN_CLIENT = "THROTTLING:FORBIDDEN_CLIENT";

  private static final String DW_HEADERS_AUTHENTICATE_CONTENT =
      "#[{'WWW-Authenticate': 'Client-ID-Enforcement'}]";

  @Override
  protected String getOnErrorContinueType() {
    return THROTTLING_FORBIDDEN_CLIENT;
  }

  @Override
  protected void setBodyElement(Element setResponseElement) {
    setResponseElement.addContent(new Element(BODY_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(DW_BODY_RESEPONSE_VALUE));
  }

  @Override
  protected void setHeadersElement(Element setResponseElement) {
    setResponseElement
        .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
            .addContent(DW_HEADERS_AUTHENTICATE_CONTENT));
  }

  @Override
  protected String getStatusCodeValue() {
    return STATUS_CODE_401;
  }
}
