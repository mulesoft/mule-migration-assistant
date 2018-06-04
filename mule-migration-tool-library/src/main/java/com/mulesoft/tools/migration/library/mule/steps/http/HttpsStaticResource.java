/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;

/**
 * Migrates the static-resource-handler of the HTTPs Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsStaticResource extends HttpStaticResource
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule//https:static-resource-handler";

  @Override
  public String getDescription() {
    return "Update HTTPs static-resource-handler.";
  }

  public HttpsStaticResource() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

}
