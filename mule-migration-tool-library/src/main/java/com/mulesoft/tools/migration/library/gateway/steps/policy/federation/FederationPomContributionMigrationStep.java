/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.federation;

import com.mulesoft.tools.migration.library.gateway.steps.policy.mule.HttpTransformPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Contribute mule federation plugin to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class FederationPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_FEDERATION_EXTENSION_ARTIFACT_ID = "mule-federation-extension";
  private static final String FEDERATION_EXTENSION_VERSION_PROPERTY = "mule-federation-extension";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";


  @Override
  public String getDescription() {
    return "Pom contribution migration step for Federation policy";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    new HttpTransformPomContributionMigrationStep().execute(pomModel, migrationReport);
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_FEDERATION_EXTENSION_ARTIFACT_ID)
        .withVersion(targetVersion(FEDERATION_EXTENSION_VERSION_PROPERTY))
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }

}
