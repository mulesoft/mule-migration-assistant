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
 * Migrate tags elements
 *
 * @author Mulesoft Inc.
 */
public class TagsTagMigrationStep extends GatewayMigrationStep {

  private static final String TAGS_TAG_NAME = "tags";

  public TagsTagMigrationStep() {
    super(API_PLATFORM_GW_MULE_3_NAMESPACE, TAGS_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    migrationReport.report("proxy.tagsTagMigrationStep", element, element);
    element.removeContent();
    element.detach();
  }
}
