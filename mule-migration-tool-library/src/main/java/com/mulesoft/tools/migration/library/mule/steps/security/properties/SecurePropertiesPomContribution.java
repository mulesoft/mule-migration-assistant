/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.properties;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the mule-secure-configuration-property-module dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurePropertiesPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add mule-secure-configuration-property-module dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.modules")
        .withArtifactId("mule-secure-configuration-property-module")
        .withVersion(targetVersion("mule-secure-configuration-property-module"))
        .withClassifier("mule-plugin")
        .build());

    object.removeDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.security")
        .withArtifactId("mule-module-security-property-placeholder")
        .withVersion("*")
        .build());

  }

}
