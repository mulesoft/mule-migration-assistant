/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the config elements of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JbossTxManager extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/jbossts' and local-name() = 'transaction-manager']";

  @Override
  public String getDescription() {
    return "Update config elements of the DB Connector.";
  }

  public JbossTxManager() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("db.jbossTxManager", object, object.getParentElement());
    object.detach();
  }
}
