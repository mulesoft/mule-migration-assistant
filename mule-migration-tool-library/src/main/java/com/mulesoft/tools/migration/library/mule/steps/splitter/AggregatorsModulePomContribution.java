/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Handles the addition of the aggregators module dependency to the app's pom
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AggregatorsModulePomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add Aggregators module dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-aggregators-module")
        .withVersion(targetVersion("mule-aggregators-module"))
        .withClassifier("mule-plugin")
        .build());
  }

}
