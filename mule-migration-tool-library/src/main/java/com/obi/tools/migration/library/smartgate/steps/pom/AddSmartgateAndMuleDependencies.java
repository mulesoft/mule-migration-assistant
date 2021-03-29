/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Removes Smartgate dependencies from pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AddSmartgateAndMuleDependencies implements PomContribution {

  @Override
  public String getDescription() {
    return "Add Smartgate and Mule dependencies";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) {
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("com.obi.smartgate.smartgate-common")
        .withArtifactId("global-error-handler-rest")
        .withVersion("1.0.0")
        .build());
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-scripting-module")
        .withVersion("2.0.0")
        .withClassifier("mule-plugin")
        .build());
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-java-module")
        .withVersion("1.2.7")
        .withClassifier("mule-plugin")
        .build());
  }
}
