/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

/**
 * Migrates the outbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsOutboundEndpoint extends HttpOutboundEndpoint {

  public static final String XPATH_SELECTOR =
      "/mule:mule//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/https' and local-name() = 'outbound-endpoint']";

  @Override
  public String getDescription() {
    return "Update HTTPs transport outbound endpoint.";
  }

  public HttpsOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

}
