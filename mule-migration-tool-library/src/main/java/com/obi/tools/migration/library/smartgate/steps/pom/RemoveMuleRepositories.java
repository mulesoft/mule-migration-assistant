/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.Repository;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Removes Smartgate dependencies from pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMuleRepositories implements PomContribution {

  @Override
  public String getDescription() {
    return "Remove MuleRepositories from pom";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) {
    for (Repository repository : pomModel.getRepositories()) {
      pomModel.removeRepository(repository);
    }
  }
}
