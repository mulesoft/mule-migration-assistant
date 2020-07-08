/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;

import org.jdom2.Element;

/**
 * Migrate unknown api error handler element
 *
 * @author Mulesoft Inc.
 */
public class UnknownApiOnErrorContinueElementWriter extends OnErrorContinueElementWriter {

  private static final String THROTTLING_UNKNOWN_API = "THROTTLING:UNKNOWN_API";

  @Override
  protected String getOnErrorContinueType() {
    return THROTTLING_UNKNOWN_API;
  }

  @Override
  protected void setBodyElement(Element setResponseElement) {
    setResponseElement.addContent(new Element(BODY_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(DW_BODY_RESEPONSE_VALUE));
  }

  @Override
  protected void setHeadersElement(Element setResponseElement) {

  }

  @Override
  protected String getStatusCodeValue() {
    return STATUS_CODE_503;
  }
}
