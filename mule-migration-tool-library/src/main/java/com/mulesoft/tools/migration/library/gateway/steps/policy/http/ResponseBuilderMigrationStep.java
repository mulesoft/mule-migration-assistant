/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.http;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate response-builder element
 *
 * @author Mulesoft Inc.
 */
public class ResponseBuilderMigrationStep extends AbstractResponseBuilderMigrationStep {

  private static final String RESPONSE_BUILDER_TAG_NAME = "response-builder";

  private static final String HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE =
      "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]";

  public ResponseBuilderMigrationStep() {
    super(HTTP_NAMESPACE, RESPONSE_BUILDER_TAG_NAME);
  }

  @Override
  protected String getListenerResponseStatusCode() {
    return HTTP_LISTENER_RESPONSE_SUCCESS_STATUS_CODE;
  }

}
