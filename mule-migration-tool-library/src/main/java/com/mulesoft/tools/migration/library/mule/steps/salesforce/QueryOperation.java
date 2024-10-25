/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

/**
 * Migrate Query operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QueryOperation extends AbstractQueryOperationMigrationStep {

  private static String name = "query";

  public QueryOperation() {
    super(name, name);
  }

  @Override
  protected void migrateHeaders(Element mule3Operation, Element mule4Operation, MigrationReport report) {
    super.migrateHeadersWithFetchSize(mule3Operation, mule4Operation, report);
  }
}
