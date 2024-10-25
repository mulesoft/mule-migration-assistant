/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.MigrationStep;

import java.nio.file.Path;

/**
 * Migration Step that works over the project structure. It should be used to move/create files or folders over the application to migrate.
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
