/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE_URI;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrates the transformers of the jms transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsTransformers extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + JMS_NAMESPACE_URI + "' and (local-name()='jmsmessage-to-object-transformer' or "
          + "local-name()='object-to-jmsmessage-transformer')]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Remove JMS tranformers.";
  }

  public JmsTransformers() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (object.getAttribute("name") != null) {
      getApplicationModel().getNodes("//mule:transformer[@ref = '" + object.getAttributeValue("name") + "']")
          .forEach(t -> t.detach());
    }
    object.detach();
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
