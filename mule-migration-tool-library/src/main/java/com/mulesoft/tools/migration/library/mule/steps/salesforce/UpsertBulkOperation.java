/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Migrate Upsert Bulk operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpsertBulkOperation extends UpsertOperation {

  private static final String m3Name = "upsert-bulk";

  public UpsertBulkOperation() {
    super();
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, m3Name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

}
