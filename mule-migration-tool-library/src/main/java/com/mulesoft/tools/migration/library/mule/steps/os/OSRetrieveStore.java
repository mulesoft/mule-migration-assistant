/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migration of Object Store Connector.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSRetrieveStore extends AbstractOSMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + OS_NAMESPACE_URI + "' and local-name() = 'retrieve-and-store']";

  public OSRetrieveStore() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Update Object Store connector.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Integer position = element.getParentElement().indexOf(element);

    addNewRetrieveOperation(element, position);
    addNewStoreOperation(element, position + 1);
    migrateConnection(element);

    element.detach();
  }
}
