/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.compression;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the Compression Module dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CompressionModulePomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add Compression module dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-compression-module")
        .withVersion(targetVersion("mule-compression-module"))
        .withClassifier("mule-plugin")
        .build());
  }
}
