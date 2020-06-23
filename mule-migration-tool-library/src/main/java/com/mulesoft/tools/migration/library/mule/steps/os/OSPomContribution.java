/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Add Object Store dependency on Pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add Object Store Connector dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("org.mule.connectors")
        .withArtifactId("mule-objectstore-connector")
        .withVersion(targetVersion("mule-objectstore-connector"))
        .withClassifier("mule-plugin")
        .build());
  }

}
