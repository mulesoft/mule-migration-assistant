/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PluginExecution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import org.apache.commons.lang3.StringUtils;

/**
 * Removes the build-helper-maven-plugin that was needed by Studio 6 from pom
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveBuildHelperMavenPlugin implements PomContribution {

  private String BUILD_HELPER_MAVEN_PLUGIN_ARTIFACT_ID = "build-helper-maven-plugin";

  @Override
  public String getDescription() {
    return "Remove build-helper-maven-plugin";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    pomModel.removePlugin(p -> studio6GeneratedHelper(p)
        && p.getExecutions().stream().allMatch(exec -> studio6GeneratedHelperExecution(exec)
            && stream(exec.getConfiguration().getChildren("resources")[0].getChildren())
                .allMatch(resource -> resource.getChildCount() == 1
                    && (resource.getChild("directory").getValue().equals("src/main/resources/")
                        || resource.getChild("directory").getValue().equals("src/main/app/")
                        || resource.getChild("directory").getValue().equals("mappings/")
                        || resource.getChild("directory").getValue().equals("src/main/api/")))));

    pomModel.getPlugins().stream().filter(p -> studio6GeneratedHelper(p)).forEach(p -> {
      p.getExecutions().stream().filter(exec -> studio6GeneratedHelperExecution(exec)).forEach(exec -> {
        stream(exec.getConfiguration().getChildren("resources")[0].getChildren())
            .filter(resource -> resource.getChild("directory").getValue().equals("src/main/app/"))
            .forEach(resource -> resource.getChild("directory").setValue("src/main/mule/"));
      });
    });
  }

  private boolean studio6GeneratedHelperExecution(PluginExecution exec) {
    return exec.getPhase().equals("generate-resources")
        && exec.getGoals().equals(singletonList("add-resource"))
        && exec.getConfiguration().getChildren().length == 1;
  }

  private boolean studio6GeneratedHelper(Plugin p) {
    return StringUtils.equals(p.getArtifactId(), BUILD_HELPER_MAVEN_PLUGIN_ARTIFACT_ID)
        && StringUtils.isBlank(p.getConfiguration().getValue())
        && p.getConfiguration().getChildCount() == 0;
  }
}
