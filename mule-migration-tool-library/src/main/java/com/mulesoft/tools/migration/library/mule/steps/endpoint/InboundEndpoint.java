/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.xml.AdditionalNamespaces.FILE;

import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the generic inbound endpoints.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule//mule:inbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update generic inbound endpoints.";
  }

  public InboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.getChildren("property", CORE_NAMESPACE).forEach(p -> {
      object.setAttribute(p.getAttributeValue("key"), p.getAttributeValue("value"));
    });
    object.removeChildren("property", CORE_NAMESPACE);

    if (object.getAttribute("address") != null) {
      String address = object.getAttributeValue("address");

      AbstractApplicationModelMigrationStep migrator = null;
      // TODO MMT-132 make available migrators discoverable
      if (address.startsWith("file://")) {
        migrator = new FileInboundEndpoint();
      }

      if (migrator != null) {
        migrator.setApplicationModel(getApplicationModel());
        if (migrator instanceof ExpressionMigratorAware) {
          ((ExpressionMigratorAware) migrator).setExpressionMigrator(getExpressionMigrator());
        }

        migrator.execute(object, report);
        object.setNamespace(Namespace.getNamespace(FILE.prefix(), FILE.uri()));
      }
      object.removeAttribute("address");
    }

    if (object.getAttribute("exchange-pattern") != null) {
      object.removeAttribute("exchange-pattern");
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
