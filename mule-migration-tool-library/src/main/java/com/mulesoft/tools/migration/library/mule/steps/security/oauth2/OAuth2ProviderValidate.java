/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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

    final Element cfg = getApplicationModel()
        .getNode("/*/*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'config']");
    element.setAttribute("config-ref", cfg.getAttributeValue("name"));

    final String scopes = element.getAttributeValue("scopes");
    if (scopes != null) {
      element.setAttribute("scopes", stream(scopes.split(" ")).collect(joining("', '", "#[['", "']]")));
    }

    if (element.getAttribute("throwExceptionOnUnaccepted") == null
        || element.getAttributeValue("throwExceptionOnUnaccepted").equals("false")) {
      report.report("filters.validationsRaiseError", element, element);
    }
    element.removeAttribute("throwExceptionOnUnaccepted");
  }

}
