/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.oauth2;

import static com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig.OAUTH2_PROVIDER_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.security.oauth2.OAuth2ProviderConfig.OAUTH2_PROVIDER_NAMESPACE_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.util.Collections.singletonList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate oauth2 provider create-client operation.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OAuth2ProviderCreateClient extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'create-client']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate oauth2 provider create-client operation.";
  }

  public OAuth2ProviderCreateClient() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(OAUTH2_PROVIDER_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    final Element cfg = getApplicationModel()
        .getNode("/*/*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'config']");
    element.setAttribute("config-ref", cfg.getAttributeValue("name"));

    migrateExpression(element.getAttribute("clientId"), getExpressionMigrator());
    migrateExpression(element.getAttribute("secret"), getExpressionMigrator());
    migrateExpression(element.getAttribute("type"), getExpressionMigrator());
    migrateExpression(element.getAttribute("principal"), getExpressionMigrator());

    final Element redirectUris = element.getChild("redirect-uris", OAUTH2_PROVIDER_NAMESPACE);
    if (redirectUris != null) {
      final Attribute redirectUrisRef = redirectUris.getAttribute("ref");
      migrateExpression(redirectUrisRef, getExpressionMigrator());
      redirectUrisRef.detach();
      element.setAttribute(redirectUrisRef.setName("redirectUris"));
      redirectUris.detach();
    }

    final Element authorizedGrantTypes = element.getChild("authorized-grant-types", OAUTH2_PROVIDER_NAMESPACE);
    if (authorizedGrantTypes != null) {
      final Attribute authorizedGrantTypesRef = authorizedGrantTypes.getAttribute("ref");
      migrateExpression(authorizedGrantTypesRef, getExpressionMigrator());
      authorizedGrantTypesRef.detach();
      element.setAttribute(authorizedGrantTypesRef.setName("authorizedGrantTypes"));
      authorizedGrantTypes.detach();
    }

    final Element scopes = element.getChild("scopes", OAUTH2_PROVIDER_NAMESPACE);
    if (scopes != null) {
      final Attribute scopesRef = scopes.getAttribute("ref");
      migrateExpression(scopesRef, getExpressionMigrator());
      scopesRef.detach();
      element.setAttribute(scopesRef.setName("scopes"));
      scopes.detach();
    }

  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
