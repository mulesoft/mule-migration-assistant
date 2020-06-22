/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Remove Schedulers namespace since is no longer needed on Mule 4.
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class RemoveSchedulersNamespace implements NamespaceContribution {

  private static final String SCHEDULER_TRANSFORMER_NAME = "schedulers";
  private static final String SCHEDULER_TRANSFORMER_URI = "http://www.mulesoft.org/schema/mule/schedulers";
  private static final String SCHEDULER_TRANSFORMER_SCHEMA =
      "http://www.mulesoft.org/schema/mule/schedulers/current/mule-schedulers.xsd";

  @Override
  public String getDescription() {
    return "Remove Schedulers namespace.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    try {
      applicationModel.removeNameSpace(SCHEDULER_TRANSFORMER_NAME, SCHEDULER_TRANSFORMER_URI, SCHEDULER_TRANSFORMER_SCHEMA);
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }
}
