/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.xml.AdditionalNamespaces.FILE;
import static com.mulesoft.tools.migration.xml.AdditionalNamespaces.HTTP;

import com.mulesoft.tools.migration.library.mule.steps.file.FileOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the generic outbound endpoints.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OutboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule//mule:outbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update generic outbound endpoints.";
  }

  public OutboundEndpoint() {
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
        migrator = new FileOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(FILE.prefix(), FILE.uri()));
      } else if (address.startsWith("http://")) {
        migrator = new HttpOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace(HTTP.prefix(), HTTP.uri()));
      } else if (address.startsWith("https://")) {
        migrator = new HttpsOutboundEndpoint();
        object.setNamespace(Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https"));
      }

      if (migrator != null) {
        migrator.setApplicationModel(getApplicationModel());
        if (migrator instanceof ExpressionMigratorAware) {
          ((ExpressionMigratorAware) migrator).setExpressionMigrator(getExpressionMigrator());
        }

        migrator.execute(object, report);
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
