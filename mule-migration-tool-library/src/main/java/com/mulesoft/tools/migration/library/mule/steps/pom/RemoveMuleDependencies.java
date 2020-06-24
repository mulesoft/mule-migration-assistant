/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import java.util.List;

/**
 * Removes mule dependencies from pom. The dependencies are selected by their group id.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMuleDependencies implements PomContribution {

  @Override
  public String getDescription() {
    return "Remove mule dependencies from pom";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) {
    List<Dependency> dependencies = pomModel.getDependencies();
    dependencies.removeIf(d -> d.getGroupId().startsWith("org.mule.") || d.getGroupId().equals("org.mule")
        || d.getGroupId().equals("com.mulesoft.anypoint") || d.getGroupId().startsWith("com.mulesoft.muleesb")
        || d.getGroupId().startsWith("com.mulesoft.weave") || d.getGroupId().startsWith("com.mulesoft.munit"));
    pomModel.setDependencies(dependencies);
  }
}
