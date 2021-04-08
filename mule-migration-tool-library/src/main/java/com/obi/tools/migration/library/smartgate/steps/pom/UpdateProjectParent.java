/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Update the version of the project to avoid conflicts with the original app
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateProjectParent implements PomContribution {


  @Override
  public String getDescription() {
    return "Update project version";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {

    final Parent parent = pomModel.getParent();
    if (parent != null) {
      parent.setArtifactId("smartgate-parent-mule-api");
      parent.setGroupId("com.obi.smartgate.maven.parent");
      parent.setVersion("2.1.0");
      pomModel.setParent(parent);
    }
  }
}
