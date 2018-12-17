/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.oauth2;

import static com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig.OAUTH2_PROVIDER_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig.OAUTH2_PROVIDER_NAMESPACE_URI;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Update oauth2 provider validate operation.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OAuth2ProviderValidate extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'validate']";

  @Override
  public String getDescription() {
    return "Update oauth2 provider validate operation.";
  }

  public OAuth2ProviderValidate() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(OAUTH2_PROVIDER_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setName("validate-token");

    final String scopes = element.getAttributeValue("scopes");
    if (scopes != null) {
      element.setAttribute("scopes", stream(scopes.split(" ")).collect(joining("', '", "#[['", "']]")));
    }
  }

}
