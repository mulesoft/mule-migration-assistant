/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.step;

import static com.google.common.base.Preconditions.checkArgument;

import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * Basic unit of execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class DefaultMigrationStep implements MigrationStep {

  private ApplicationModel applicationModel;

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }
}
