/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.ipfilter;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Contribute mule-ip-filter plugin to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class IpFilterPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String MULE_IP_FILTER_EXTENSION_ARTIFACT_ID = "mule-ip-filter-extension";
  private static final String EXTENSION_VERSION_PROPERTY = "mule-ip-filter-extension";

  @Override
  public String getDescription() {
    return "Pom contribution migration step for IP Filter policy";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_IP_FILTER_EXTENSION_ARTIFACT_ID)
        .withVersion(targetVersion(EXTENSION_VERSION_PROPERTY))
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
