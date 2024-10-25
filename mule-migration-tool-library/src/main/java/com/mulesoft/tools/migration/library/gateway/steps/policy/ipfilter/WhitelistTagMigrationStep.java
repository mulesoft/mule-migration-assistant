/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.IP_FILTER_GW_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate whitelist element
 *
 * @author Mulesoft Inc.
 */
public class WhitelistTagMigrationStep extends AbstractIpFilterMigrationStep {

  private static final String WHITELIST_TAG_NAME = "whitelist";
  private static final String CONFIG_REF_ATTR_VALUE = "whitelist_config";

  public WhitelistTagMigrationStep() {
    super(IP_FILTER_GW_NAMESPACE, WHITELIST_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    migrateBlacklistWhitelistElement(element, CONFIG_REF_ATTR_VALUE);
  }
}
