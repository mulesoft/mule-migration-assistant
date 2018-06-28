/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;

import com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsInboundEndpoint;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
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

  private static final String HTTP_NS_PREFIX = "http";
  private static final String HTTP_NS_URI = "http://www.mulesoft.org/schema/mule/http";
  private static final String FILE_NS_PREFIX = "file";
  private static final String FILE_NS_URI = "http://www.mulesoft.org/schema/mule/file";
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

    object.setAttribute("isMessageSource", "true");

    AbstractApplicationModelMigrationStep migrator = null;

    if (object.getAttribute("address") != null) {
      String address = object.getAttributeValue("address");

      // TODO MMT-132 make available migrators discoverable
      if (address.startsWith("file://")) {
        migrator = new FileInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
      } else if (address.startsWith("http://")) {
        migrator = new HttpInboundEndpoint();
        object.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
      }

      if (migrator != null) {
        migrator.setApplicationModel(getApplicationModel());
        if (migrator instanceof ExpressionMigratorAware) {
          ((ExpressionMigratorAware) migrator).setExpressionMigrator(getExpressionMigrator());
        }

        migrator.execute(object, report);
      }
      object.removeAttribute("address");
    } else if (object.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/mule:mule/*[@name = '" + object.getAttributeValue("ref") + "']");

      // TODO MMT-132 make available migrators discoverable
      if (globalEndpoint.getAttribute("address") != null) {
        String address = globalEndpoint.getAttributeValue("address");
        if (address.startsWith("file://")) {
          migrator = new FileInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(FILE_NS_PREFIX, FILE_NS_URI));
        } else if (address.startsWith("http://")) {
          migrator = new HttpInboundEndpoint();
          object.setNamespace(Namespace.getNamespace(HTTP_NS_PREFIX, HTTP_NS_URI));
        } else if (address.startsWith("https://")) {
          migrator = new HttpsInboundEndpoint();
          object.setNamespace(Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https"));
        }

        if (migrator != null) {
          migrator.setApplicationModel(getApplicationModel());
          if (migrator instanceof ExpressionMigratorAware) {
            ((ExpressionMigratorAware) migrator).setExpressionMigrator(getExpressionMigrator());
          }

          for (Attribute attribute : globalEndpoint.getAttributes()) {
            if (object.getAttribute(attribute.getName()) == null) {
              object.setAttribute(attribute.getName(), attribute.getValue());
            }
          }

          migrator.execute(object, report);
        }
      }
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
