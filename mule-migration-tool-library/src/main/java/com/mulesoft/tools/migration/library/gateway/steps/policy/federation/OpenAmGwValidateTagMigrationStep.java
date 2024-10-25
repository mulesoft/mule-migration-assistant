/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.federation;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.OPENAM_GW_NAMESPACE;

/**
 * Migrate authenticate-openam element
 *
 * @author Mulesoft Inc.
 */
public class OpenAmGwValidateTagMigrationStep extends AbstractFederationValidateMigrationStep {

  private static final String AUTHENTICATE_OPENAM_TAG_NAME = "authenticate-openam";
  private static final String OPENAM_CONFIG_TAG_NAME = "open-am-config";
  private static final String HEADERS_AUTHENTICATE_CONTENT = "#[{'WWW-Authenticate': 'Bearer realm=\"OpenAM Client Realm\"'}]";

  public OpenAmGwValidateTagMigrationStep() {
    super(OPENAM_GW_NAMESPACE, VALIDATE_TAG_NAME);
  }

  @Override
  protected String getConfigElementTagName() {
    return OPENAM_CONFIG_TAG_NAME;
  }

  @Override
  protected String getAuthenticateElementTagName() {
    return AUTHENTICATE_OPENAM_TAG_NAME;
  }

  @Override
  protected String getHeadersAuthenticateContent() {
    return HEADERS_AUTHENTICATE_CONTENT;
  }
}
