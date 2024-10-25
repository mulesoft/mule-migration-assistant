/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_PLATFORM_GW_MULE_3_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate description tag
 *
 * @author Mulesoft Inc.
 */
public class DescriptionTagMigrationStep extends GatewayMigrationStep {

  private static final String DESCRIPTION_TAG_NAME = "description";

  public DescriptionTagMigrationStep() {
    super(API_PLATFORM_GW_MULE_3_NAMESPACE, DESCRIPTION_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    migrationReport.report("proxy.descriptionTagMigrationStep", element, element);
    element.removeContent();
    element.detach();
  }
}
