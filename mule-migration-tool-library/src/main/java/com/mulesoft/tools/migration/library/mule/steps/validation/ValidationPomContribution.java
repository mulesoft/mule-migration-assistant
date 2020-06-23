/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Add validation module dependency on pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add validation module to pom.";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    addValidationDependency(pomModel);
  }

  public static void addValidationDependency(PomModel pomModel) {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-validation-module")
        .withVersion(targetVersion("mule-validation-module"))
        .withClassifier("mule-plugin")
        .build());
  }
}
