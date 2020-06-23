/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.addSharedLibs;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the DB Connector dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbConnectorPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add DB Connector dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.connectors")
        .withArtifactId("mule-db-connector")
        .withVersion(targetVersion("mule-db-connector"))
        .withClassifier("mule-plugin")
        .build());

    // Due to licensing issues, we may only know the actual GAV of the derby driver.
    object.getDependencies().stream().filter(dep -> "org.apache.derby".equals(dep.getGroupId()))
        .forEach(dep -> addSharedLibs(object, dep));
  }
}
