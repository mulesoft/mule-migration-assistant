/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate federation not authorized error handler element
 *
 * @author Mulesoft Inc.
 */
public class FederationNotAuthorizedConnectionErrorOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String FEDERATION_NOT_AUTHORIZED_CONNECTION_ERROR_TYPE =
      "FEDERATION:NOT_AUTHORIZED, FEDERATION:CONNECTION_ERROR";

  private final String headersAuthenticateContent;

  public FederationNotAuthorizedConnectionErrorOnErrorContinueElementWriter(String headersAuthenticaContent) {
    this.headersAuthenticateContent = headersAuthenticaContent;
  }

  @Override
  protected String getOnErrorContinueType() {
    return FEDERATION_NOT_AUTHORIZED_CONNECTION_ERROR_TYPE;
  }

  @Override
  protected void setBodyElement(Element setResponseElement) {
    setResponseElement.addContent(new Element(BODY_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(DW_BODY_RESEPONSE_VALUE));
  }

  @Override
  protected void setHeadersElement(Element setResponseElement) {
    setResponseElement
        .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE)
            .addContent(headersAuthenticateContent));
  }

  @Override
  protected String getStatusCodeValue() {
    return STATUS_CODE_401;
  }
}
