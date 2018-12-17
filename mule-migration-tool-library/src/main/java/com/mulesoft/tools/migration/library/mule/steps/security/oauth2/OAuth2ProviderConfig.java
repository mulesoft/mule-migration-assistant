/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.oauth2;

import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Update oauth2 provider configuration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OAuth2ProviderConfig extends AbstractApplicationModelMigrationStep {

  public static final String OAUTH2_PROVIDER_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/oauth2-provider";
  public static final Namespace OAUTH2_PROVIDER_NAMESPACE = getNamespace("oauth2-provider", OAUTH2_PROVIDER_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'config']";

  // private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update oauth2 provider configuration.";
  }

  public OAuth2ProviderConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(OAUTH2_PROVIDER_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(OAUTH2_PROVIDER_NAMESPACE,
                                       "http://www.mulesoft.org/schema/mule/oauth2-provider/current/mule-oauth2-provider.xsd",
                                       element.getDocument());

    element.getAttribute("prioviderName").setName("name");

    final Element authorizationConfig = new Element("authorization-config", OAUTH2_PROVIDER_NAMESPACE);

    if (element.getAttributeValue("loginPage") != null) {
      authorizationConfig.setAttribute("loginPage", element.getAttributeValue("loginPage"));
      element.removeAttribute("loginPage");
    }
    final String path = element.getAttributeValue("path");
    if (path != null) {
      authorizationConfig.setAttribute("path", path.startsWith("/") ? path : "/" + path);
      element.removeAttribute("path");
    }
    if (element.getAttributeValue("authorizationCodeStore-ref") != null) {
      authorizationConfig.setAttribute("authorizationCodeStore", element.getAttributeValue("authorizationCodeStore-ref"));
      element.removeAttribute("authorizationCodeStore-ref");
    }

    element.addContent(authorizationConfig);

  }

}
