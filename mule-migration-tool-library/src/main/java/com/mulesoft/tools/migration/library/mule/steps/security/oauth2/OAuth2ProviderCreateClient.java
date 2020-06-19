/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
