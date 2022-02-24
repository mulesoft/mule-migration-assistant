/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.clientidenforcement;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Add Client Id enforcement plugin to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class ClientIdEnforcementPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String MULE_CLIENT_ID_ENFORCEMENT_EXTENSION_ARTIFACT_ID = "mule-client-id-enforcement-extension";
  private static final String CLIENT_ID_ENFORCEMENT_EXTENSION_VERSION_PROPERTY = "mule-client-enforcement-extension";

  @Override
  public String getDescription() {
    return "Pom contribution migration step for Client ID Enforcement policy";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_CLIENT_ID_ENFORCEMENT_EXTENSION_ARTIFACT_ID)
        .withVersion(targetVersion(CLIENT_ID_ENFORCEMENT_EXTENSION_VERSION_PROPERTY))
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
