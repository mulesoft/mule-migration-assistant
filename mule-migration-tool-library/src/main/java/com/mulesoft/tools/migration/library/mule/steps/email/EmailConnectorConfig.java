/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
