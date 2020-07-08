/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.federation;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.OAUTH2_GW_NAMESPACE;

/**
 * Migrate authenticate-oauth2 element
 *
 * @author Mulesoft Inc.
 */
public class Oauth2GwValidateTagMigrationStep extends AbstractFederationValidateMigrationStep {

  private static final String AUTHENTICATE_OAUTH2_TAG_NAME = "authenticate-oauth2";
  private static final String OAUTH2_CONFIG_TAG_NAME = "oauth2-config";
  private static final String HEADERS_AUTHENTICATE_CONTENT = "#[{'WWW-Authenticate': 'Bearer realm=\"OAuth2 Client Realm\"'}]";

  public Oauth2GwValidateTagMigrationStep() {
    super(OAUTH2_GW_NAMESPACE, VALIDATE_TAG_NAME);
  }

  @Override
  protected String getConfigElementTagName() {
    return OAUTH2_CONFIG_TAG_NAME;
  }

  @Override
  protected String getAuthenticateElementTagName() {
    return AUTHENTICATE_OAUTH2_TAG_NAME;
  }

  @Override
  protected String getHeadersAuthenticateContent() {
    return HEADERS_AUTHENTICATE_CONTENT;
  }
}
