/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Migrate policy deploy properties on pom.xml
 *
 * @author Mulesoft Inc.
 */
public class AddMavenPomContributionMigrationStep implements PomContribution {

  private static final String MAVEN_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
  private static final String MAVEN_CLEAN_PLUGIN_ARTIFACT_ID = "mmaven-clean-plugin";


  @Override
  public String getDescription() {
    return "Pom Contribution to add properties for deploying to Exchange";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    addPlugins(pomModel);
  }


  private void addPlugins(PomModel pomModel) {
    // pomModel.addPlugin(getMavenDeployPlugin());
  }



  private Plugin getMavenDeployPlugin() {
    Plugin plugin = new Plugin();
    plugin.setGroupId(MAVEN_PLUGIN_GROUP_ID);
    plugin.setArtifactId(MAVEN_CLEAN_PLUGIN_ARTIFACT_ID);
    plugin.setVersion("3.0.0");
    return plugin;
  }

}
