/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.federation;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.OPENIDCONNECT_GW_NAMESPACE;

/**
 * Migrate authenticate-open-id element
 *
 * @author Mulesoft Inc.
 */
public class OpenIdConnectGwValidateTagMigrationStep extends AbstractFederationValidateMigrationStep {

  private static final String AUTHENTICATE_OPENID_TAG_NAME = "authenticate-open-id";
  private static final String OPENID_CONFIG_TAG_NAME = "open-id-config";
  private static final String HEADERS_AUTHENTICATE_CONTENT = "#[{'WWW-Authenticate': 'Bearer realm=\"OpenId Client Realm\"'}]";

  public OpenIdConnectGwValidateTagMigrationStep() {
    super(OPENIDCONNECT_GW_NAMESPACE, VALIDATE_TAG_NAME);
  }

  @Override
  protected String getConfigElementTagName() {
    return OPENID_CONFIG_TAG_NAME;
  }

  @Override
  protected String getAuthenticateElementTagName() {
    return AUTHENTICATE_OPENID_TAG_NAME;
  }

  @Override
  protected String getHeadersAuthenticateContent() {
    return HEADERS_AUTHENTICATE_CONTENT;
  }
}
