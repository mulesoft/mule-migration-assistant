/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrates the Pop3 inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Pop3InboundEndpoint extends AbstractEmailMigrator
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/*/mule:flow/pop3:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Pop3 transport inbound endpoint.";
  }

  public Pop3InboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {}

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
