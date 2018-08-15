/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;

/**
 * Migrates the generic inbound endpoints.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InboundEndpoint extends AbstractEndPointMigration
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

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    AbstractApplicationModelMigrationStep migrator;

    if (object.getAttribute("address") != null) {
      String address = object.getAttributeValue("address");
      migrator = getInboundMigrator(address, object);
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

      if (globalEndpoint.getAttribute("address") != null) {
        String address = globalEndpoint.getAttributeValue("address");
        migrator = getInboundMigrator(address, object);
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
