/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.throttling;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_GW_MULE_3_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate throttle element
 *
 * @author Mulesoft Inc.
 */
public class ThrottleTagMigrationStep extends AbstractThrottlingMigrationStep {

  public ThrottleTagMigrationStep() {
    super(THROTTLING_GW_MULE_3_NAMESPACE, THROTTLE_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.removeContent();
    element.detach();
  }
}
