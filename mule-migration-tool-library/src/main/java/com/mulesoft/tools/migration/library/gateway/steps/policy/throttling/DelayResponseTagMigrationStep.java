/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.throttling;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate delay response element
 *
 * @author Mulesoft Inc.
 */
public class DelayResponseTagMigrationStep extends AbstractThrottlingMigrationStep {

  public DelayResponseTagMigrationStep() {
    super(THROTTLING_GW_MULE_3_NAMESPACE, DELAY_RESPONSE_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.removeContent();
    element.detach();
  }
}
