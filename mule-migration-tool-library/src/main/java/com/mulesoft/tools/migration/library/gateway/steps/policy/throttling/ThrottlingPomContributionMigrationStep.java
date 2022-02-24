/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.throttling;

import com.mulesoft.tools.migration.library.gateway.steps.policy.mule.HttpTransformPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Contribute throttling plugin to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class ThrottlingPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_THROTTLING_EXTENSION_ARTIFACT_ID = "mule-throttling-extension";
  private static final String THROTTLING_EXTENSION_VERSION_PROPERTY = "mule-throttling-extension";
  private static final String THROTTLING_EXTENSION_SLA_VERSION_PROPERTY = "mule-throttling-sla-extension";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  @Override
  public String getDescription() {
    return "Pom contribution migration step for Throttling policy";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    new HttpTransformPomContributionMigrationStep().execute(pomModel, migrationReport);
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_THROTTLING_EXTENSION_ARTIFACT_ID)
        .withVersion(targetVersion(THROTTLING_EXTENSION_VERSION_PROPERTY))
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
