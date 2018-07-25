/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;

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
    pomModel.removePlugin(p -> StringUtils.equals(p.getArtifactId(), BUILD_HELPER_MAVEN_PLUGIN_ARTIFACT_ID)
        && StringUtils.isBlank(p.getConfiguration().getValue())
        && p.getConfiguration().getChildCount() == 0
        && p.getExecutions().stream().allMatch(exec -> exec.getPhase().equals("generate-resources")
            && exec.getGoals().equals(singletonList("add-resource"))
            && exec.getConfiguration().getChildren().length == 1
            && stream(exec.getConfiguration().getChildren("resources")[0].getChildren())
                .allMatch(resource -> resource.getChild("directory").getValue().equals("src/main/resources/")
                    || resource.getChild("directory").getValue().equals("src/main/app/")
                    || resource.getChild("directory").getValue().equals("mappings/")
                    || resource.getChild("directory").getValue().equals("src/main/api/"))));
  }
}
