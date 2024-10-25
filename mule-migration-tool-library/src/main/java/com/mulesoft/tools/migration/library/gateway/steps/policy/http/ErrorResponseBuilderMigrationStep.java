/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.http;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate error-response-builder element
 *
 * @author Mulesoft Inc.
 */
public class ErrorResponseBuilderMigrationStep extends AbstractResponseBuilderMigrationStep {

  private static final String ERROR_RESPONSE_BUILDER_TAG_NAME = "error-response-builder";

  private static final String HTTP_LISTENER_RESPONSE_ERROR_STATUS_CODE =
      "#[vars.statusCode default migration::HttpListener::httpListenerResponseErrorStatusCode(vars)]";

  public ErrorResponseBuilderMigrationStep() {
    super(HTTP_NAMESPACE, ERROR_RESPONSE_BUILDER_TAG_NAME);
  }

  @Override
  protected String getListenerResponseStatusCode() {
    return HTTP_LISTENER_RESPONSE_ERROR_STATUS_CODE;
  }
}
