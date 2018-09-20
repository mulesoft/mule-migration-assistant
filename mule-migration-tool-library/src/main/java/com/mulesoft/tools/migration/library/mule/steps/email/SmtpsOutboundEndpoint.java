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
 * Migrates the outbound smtps endpoint of the email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SmtpsOutboundEndpoint extends SmtpOutboundEndpoint {

  public static final String XPATH_SELECTOR = "//smtps:outbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update SMTPs transport outbound endpoint.";
  }

  public SmtpsOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    super.execute(object, report);
  }

}
