/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Set the project description with information about the migration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetSmartgateProjectDescription implements PomContribution {


  @Override
  public String getDescription() {
    return "Set the project description ";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    String name = pomModel.getName();
    if (name != null) {
      pomModel.setDescription(name + "Application migrated with MMA");
    } else {
      pomModel.setDescription(pomModel.getArtifactId() + "Application migrated with MMA");
    }
  }

}
