/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.mule;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Contribute http-policy-transform plugin to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class HttpTransformPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID = "mule-http-policy-transform-extension";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String HTTP_TRANSFORM_EXTENSION_VERSION = "3.0.0";

  @Override
  public String getDescription() {
    return "Pom contribution migration step for HTTP transform element";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId(COM_MULESOFT_ANYPOINT_GROUP_ID)
        .withArtifactId(MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID)
        .withVersion(HTTP_TRANSFORM_EXTENSION_VERSION)
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
