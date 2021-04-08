/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import org.apache.commons.lang3.StringUtils;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Removes the Mule App Maven Plugin from pom
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveMavenPlugins implements PomContribution {

  private static final String MAVEN_DEPENDENCY_PLUGIN = "maven-dependency-plugin";
  private static final String SMARTGATE_MULE_VALIDATION_MAVEN_PLUGIN = "smartgate-mule-validation-maven-plugin";


  @Override
  public String getDescription() {
    return "Remove mule-app-maven-plugin";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {

    pomModel.removePlugin(p -> StringUtils.equals(p.getArtifactId(), SMARTGATE_MULE_VALIDATION_MAVEN_PLUGIN));
    pomModel.removePlugin(p -> StringUtils.equals(p.getArtifactId(), MAVEN_DEPENDENCY_PLUGIN));

    pomModel.getProfiles().stream().map(profile -> profile.getBuild()).forEach(b -> {
      b.getPlugins().removeIf(p -> StringUtils.equals(p.getArtifactId(), SMARTGATE_MULE_VALIDATION_MAVEN_PLUGIN));
    });
  }
}
