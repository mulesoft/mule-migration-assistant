/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import java.util.Optional;

/**
 * Update the version of the project to avoid conflicts with the original app
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateProjectParent implements PomContribution {

  ApplicationModel applicationModel;

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel appModel) {
    this.applicationModel = appModel;
  }

  @Override
  public String getDescription() {
    return "Update project version";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {

    final Optional<String> projectParentGAV = getApplicationModel().getProjectParentGAV();
    final Optional<Parent> optionalParent = pomModel.getParent();
    if (optionalParent.isPresent() && projectParentGAV.isPresent()) {
      String[] gav = projectParentGAV.get().split(":");
      Parent parent = optionalParent.get();
      parent.setGroupId(gav[0]);
      parent.setArtifactId(gav[1]);
      parent.setVersion(gav[2]);
      pomModel.setParent(parent);
    }
  }
}
