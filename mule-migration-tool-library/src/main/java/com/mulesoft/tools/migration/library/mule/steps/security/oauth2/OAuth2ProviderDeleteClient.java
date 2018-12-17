/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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

import org.jdom2.Element;

/**
 * Migrate oauth2 provider delete-client operation.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OAuth2ProviderDeleteClient extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'delete-client']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Migrate oauth2 provider delete-client operation.";
  }

  public OAuth2ProviderDeleteClient() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(OAUTH2_PROVIDER_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    final Element cfg = getApplicationModel()
        .getNode("/*/*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'config']");
    element.setAttribute("config-ref", cfg.getAttributeValue("name"));

    migrateExpression(element.getAttribute("clientId"), getExpressionMigrator());
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
