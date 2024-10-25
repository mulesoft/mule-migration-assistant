/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

/**
 * Migrate Non Paginated Query operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class NonPaginatedQueryOperation extends AbstractQueryOperationMigrationStep {

  private static String m3Name = "non-paginated-query";
  private static String m4Name = "query";

  public NonPaginatedQueryOperation() {
    super(m3Name, m4Name);
  }

}
