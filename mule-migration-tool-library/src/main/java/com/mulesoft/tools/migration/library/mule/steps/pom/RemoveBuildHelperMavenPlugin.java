/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
