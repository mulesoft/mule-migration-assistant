/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.federation;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.PINGFEDERATE_GW_NAMESPACE;

/**
 * Migrate authenticate-ping-federate element
 *
 * @author Mulesoft Inc.
 */
public class PingFederateGwValidateTagMigrationStep extends AbstractFederationValidateMigrationStep {

  private static final String AUTHENTICATE_PINGFEDERATE_TAG_NAME = "authenticate-ping-federate";
  private static final String PINGFEDERATE_CONFIG_TAG_NAME = "ping-federate-config";
  private static final String HEADERS_AUTHENTICATE_CONTENT =
      "#[{'WWW-Authenticate': 'Bearer realm=\"PingFederate Client Realm\"'}]";

  public PingFederateGwValidateTagMigrationStep() {
    super(PINGFEDERATE_GW_NAMESPACE, VALIDATE_TAG_NAME);
  }

  @Override
  protected String getConfigElementTagName() {
    return PINGFEDERATE_CONFIG_TAG_NAME;
  }

  @Override
  protected String getAuthenticateElementTagName() {
    return AUTHENTICATE_PINGFEDERATE_TAG_NAME;
  }

  @Override
  protected String getHeadersAuthenticateContent() {
    return HEADERS_AUTHENTICATE_CONTENT;
  }
}
