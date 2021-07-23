/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.steps;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds APIkit for SOAP dependency
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SoapkitMigrationTaskPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add APIkit for SOAP dependency";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-soapkit-module")
        .withVersion(targetVersion("mule-soapkit-module"))
        .withClassifier("mule-plugin")
        .build());
  }

}
