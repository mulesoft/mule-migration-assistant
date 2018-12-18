/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2PomContribution;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderCreateClient;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderRevokeToken;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderStoresConfigRemove;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderValidate;
import com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderValidateClient;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migrate Security Module OAuth2 Provider
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurityOAuth2ProviderMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Security Module OAuth2 Provider";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new OAuth2PomContribution(),
                        new OAuth2ProviderConfig(),
                        new OAuth2ProviderValidate(),
                        new OAuth2ProviderRevokeToken(),
                        new OAuth2ProviderValidateClient(),
                        new OAuth2ProviderCreateClient(),
                        new OAuth2ProviderStoresConfigRemove());
  }
}
