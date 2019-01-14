/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Removes the already migrated email transport connectors
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class EmailConnectorConfig extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/*[("
          + "namespace-uri() = 'http://www.mulesoft.org/schema/mule/imap' or "
          + "namespace-uri() = 'http://www.mulesoft.org/schema/mule/imaps' or "
          + "namespace-uri() = 'http://www.mulesoft.org/schema/mule/pop3' or "
          + "namespace-uri() = 'http://www.mulesoft.org/schema/mule/pop3s' or "
          + "namespace-uri() = 'http://www.mulesoft.org/schema/mule/smtp' or "
          + "namespace-uri() = 'http://www.mulesoft.org/schema/mule/smtps'"
          + ") and (local-name() = 'connector' or local-name()='gmail-connector')]";

  @Override
  public String getDescription() {
    return "Update http and https connector config.";
  }

  public EmailConnectorConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
