/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.MigrationStep;

/**
 * Migration Step that contributes to the pom model. It should be used if dependencies, plugins or repositories should be added/removed to/from the pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface PomContribution extends MigrationStep<PomModel> {

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
