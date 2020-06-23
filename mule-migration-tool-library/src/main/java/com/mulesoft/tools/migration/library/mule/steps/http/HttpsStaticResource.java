/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE_URI;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;

/**
 * Migrates the static-resource-handler of the HTTPs Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsStaticResource extends HttpStaticResource
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + HTTPS_NAMESPACE_URI + "' and local-name()='static-resource-handler']";

  @Override
  public String getDescription() {
    return "Update HTTPs static-resource-handler.";
  }

  public HttpsStaticResource() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

}
