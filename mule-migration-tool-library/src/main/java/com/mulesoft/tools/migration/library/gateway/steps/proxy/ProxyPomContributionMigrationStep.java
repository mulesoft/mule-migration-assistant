/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

/**
 * Contribute mule-http-proxy and mule-wsdl-functions plugins to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class ProxyPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID = "mule-http-proxy-extension";
  private static final String MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID = "mule-wsdl-functions-extension";
  private static final String HTTP_PROXY_EXTENSION_VERSION_PROPERTY = "mule-http-proxy-extension";
  private static final String WSDL_FUNCTIONS_EXTENSION_VERSION_PROPERTY = "mule-wsdl-functions-extension";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  private boolean isCustomProcessorMigrationStep;

  public ProxyPomContributionMigrationStep(boolean isCustomProcessorMigrationStep) {
    this.isCustomProcessorMigrationStep = isCustomProcessorMigrationStep;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    Dependency.DependencyBuilder dependencyBuilder = new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .withArtifactId(
                        this.isCustomProcessorMigrationStep ? MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID
                            : MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID)
        .withVersion(this.isCustomProcessorMigrationStep ? targetVersion(HTTP_PROXY_EXTENSION_VERSION_PROPERTY)
            : targetVersion(WSDL_FUNCTIONS_EXTENSION_VERSION_PROPERTY));

    pomModel.addDependency(dependencyBuilder.build());
  }
}
