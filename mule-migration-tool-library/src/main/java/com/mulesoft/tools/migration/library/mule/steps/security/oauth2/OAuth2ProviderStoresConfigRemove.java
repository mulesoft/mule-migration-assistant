/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.oauth2;

import static com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig.OAUTH2_PROVIDER_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringBeans.SPRING_BEANS_NS_URI;
import static java.util.Collections.singletonList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove spring object store definitions for oauth2 provider.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OAuth2ProviderStoresConfigRemove extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + SPRING_BEANS_NS_URI + "' and local-name() = 'bean' "
          + "and (@class = 'org.mule.modules.oauth2.provider.client.ObjectStoreClientStore' "
          + "or @class = 'org.mule.modules.oauth2.provider.token.ManagedObjectStoreTokenStore' "
          + "or @class = 'org.mule.modules.oauth2.provider.code.ObjectStoreAuthorizationCode' "
          + "or @class = 'org.mule.modules.oauth2.provider.ratelimit.SimpleInMemoryRateLimiter')]";

  @Override
  public String getDescription() {
    return "Remove spring object store definitions for oauth2 provider.";
  }

  public OAuth2ProviderStoresConfigRemove() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(OAUTH2_PROVIDER_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.detach();
  }

}
