/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Adds the Salesforce Connector dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SalesforcePomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add Salesforce Connector dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.connector")
        .withArtifactId("mule-salesforce-connector")
        .withVersion(targetVersion("mule-salesforce-connector"))
        .withClassifier("mule-plugin")
        .build());
  }
}
