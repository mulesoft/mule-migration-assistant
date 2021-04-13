/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import org.jdom2.Element;

/**
 * Migrate Cached Basic configuration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CachedBasicConfiguration extends AbstractSalesforceConfigurationMigrationStep implements ExpressionMigratorAware {

  private static final String MULE3_NAME = "cached-basic-config";
  private static final String MULE4_CONFIG = "sfdc-config";
  private static final String MULE4_NAME = "basic-connection";

  public CachedBasicConfiguration() {
    super(MULE4_CONFIG, MULE4_NAME);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, MULE3_NAME, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3CachedBasicConfig, MigrationReport report) throws RuntimeException {
    super.execute(mule3CachedBasicConfig, report);

    XmlDslUtils.addElementAfter(mule4Config, mule3CachedBasicConfig);
    mule3CachedBasicConfig.getParentElement().removeContent(mule3CachedBasicConfig);
  }
}
