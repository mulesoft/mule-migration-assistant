/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrates the pop3s inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Pop3sInboundEndpoint extends Pop3InboundEndpoint {

  public static final String XPATH_SELECTOR = "/*/mule:flow/pop3s:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Pop3s transport inbound endpoint.";
  }

  public Pop3sInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    super.execute(object, report);
  }

}
