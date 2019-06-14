/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.MigrationStep;

import java.nio.file.Path;

/**
 * Migration Step that works over the project structure.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ProjectStructureContribution extends MigrationStep<Path> {

  /**
   * Retrieves the application model that this contribution step is working over.
   *
   * @return a {@link ApplicationModel}
   */
  default ApplicationModel getApplicationModel() {
    return null;
  }

  /**
   * Sets the application model on which this contribution step should work over.
   *
   * @return a {@link ApplicationModel}
   */
  default void setApplicationModel(ApplicationModel appModel) {
    // Nothing to do by default
  }
}
