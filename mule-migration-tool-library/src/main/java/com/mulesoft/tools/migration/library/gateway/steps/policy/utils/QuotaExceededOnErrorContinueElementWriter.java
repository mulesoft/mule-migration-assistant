/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate quota exceeded error handler element
 *
 * @author Mulesoft Inc.
 */
public class QuotaExceededOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String THROTTLING_QUOTA_EXCEEDED = "THROTTLING:QUOTA_EXCEEDED";

  @Override
  protected String getOnErrorContinueType() {
    return THROTTLING_QUOTA_EXCEEDED;
  }

  @Override
  protected void setBodyElement(Element setResponseElement) {
    setResponseElement.addContent(new Element(BODY_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(DW_BODY_RESEPONSE_VALUE));
  }

  @Override
  protected void setHeadersElement(Element setResponseElement) {
    setResponseElement
        .addContent(new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(DW_HEADERS_EXCEPTION_RESPONSE_VALUE));
  }

  @Override
  protected String getStatusCodeValue() {
    return STATUS_CODE_429;
  }

}
