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

/**
 * Contribute throttling plugin to pom.xml
 *
 * @author Mulesoft Inc.
 */
public class ThrottlingPomContributionMigrationStep implements PomContribution {

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_THROTTLING_EXTENSION_ARTIFACT_ID = "mule-throttling-extension";
  private static final String THROTTLING_EXTENSION_VERSION = "1.2.2";
  private static final String THROTTLING_EXTENSION_SLA_VERSION = "1.1.4";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

  private final boolean isSla;

  public ThrottlingPomContributionMigrationStep(boolean isSla) {
    this.isSla = isSla;
  }


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
        .withVersion(isSla ? THROTTLING_EXTENSION_SLA_VERSION : THROTTLING_EXTENSION_VERSION)
        .withClassifier(MULE_PLUGIN_CLASSIFIER)
        .build());
  }
}
