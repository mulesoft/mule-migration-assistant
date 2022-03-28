/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import com.google.common.collect.ImmutableList;
import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.AbstractUnsupportedElementsMigrationStep;

import java.util.List;

/**
 * Specific Unsupported Element migration step for Salesforce Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SalesforceUnsupportedElementsStep extends AbstractUnsupportedElementsMigrationStep {

  public SalesforceUnsupportedElementsStep() {
    super(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE);
  }

  @Override
  public List<String> getUnsupportedElements() {
    return ImmutableList.of("config-oauth-user-pass");
  }
}
