/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate federation bad response error handler element
 *
 * @author Mulesoft Inc.
 */
public class FederationBadResponseOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String FEDERATION_BAD_RESPONSE_TYPE = "FEDERATION:BAD_RESPONSE_ERROR";

  @Override
  protected String getOnErrorContinueType() {
    return FEDERATION_BAD_RESPONSE_TYPE;
  }

  @Override
  protected void setBodyElement(Element setResponseElement) {
    setResponseElement.addContent(new Element(BODY_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(DW_BODY_RESEPONSE_VALUE));
  }

  @Override
  protected void setHeadersElement(Element setResponseElement) {}

  @Override
  protected String getStatusCodeValue() {
    return STATUS_CODE_500;
  }

}
