/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import org.apache.commons.lang3.StringUtils;

/**
 * Removes the Mule App Maven Plugin from pom
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMuleAppMavenPlugin implements PomContribution {

  private String MULE_APP_MAVEN_PLUGIN_ARTIFACT_ID = "mule-app-maven-plugin";

  @Override
  public String getDescription() {
    return "Remove mule-app-maven-plugin";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    pomModel.removePlugin(p -> StringUtils.equals(p.getArtifactId(), MULE_APP_MAVEN_PLUGIN_ARTIFACT_ID));
    pomModel.getProfiles().stream().map(profile -> profile.getBuild()).forEach(b -> {
      b.getPlugins().removeIf(p -> StringUtils.equals(p.getArtifactId(), MULE_APP_MAVEN_PLUGIN_ARTIFACT_ID));
    });
  }
}
