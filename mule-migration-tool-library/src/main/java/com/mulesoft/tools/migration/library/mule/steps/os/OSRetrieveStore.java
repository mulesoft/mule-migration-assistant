/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
