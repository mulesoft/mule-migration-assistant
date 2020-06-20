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
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PluginExecution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adds the HTTP Connector dependency
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MUnitPomContribution implements PomContribution {

  private static final String MUNIT_MAVEN_PLUGIN_GROUP_ID = "com.mulesoft.munit.tools";
  private static final String MUNIT_MAVEN_PLUGIN_ARTIFACT_ID = "munit-maven-plugin";
  private static final String MUNIT_SUPPORT_PROPERTY = "mule.munit.support.version";
  private static final String MUNIT_PROPERTY = "munit.version";

  @Override
  public String getDescription() {
    return "Add MUnit dependencies.";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.munit")
        .withArtifactId("munit-runner")
        .withVersion(targetVersion("munit-maven-plugin"))
        .withClassifier("mule-plugin")
        .withScope("test")
        .build());

    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.munit")
        .withArtifactId("munit-tools")
        .withVersion(targetVersion("munit-maven-plugin"))
        .withClassifier("mule-plugin")
        .withScope("test")
        .build());

    if (!getMUnitPlugin(pomModel).isEmpty()) {
      Plugin munitPlugin = getMUnitPlugin(pomModel).get(0);
      munitPlugin.setVersion(targetVersion("munit-maven-plugin"));
    } else {
      pomModel.addPlugin(buildMunitPlugin());
    }

    pomModel.removeProperty(MUNIT_SUPPORT_PROPERTY);
    pomModel.removeProperty(MUNIT_PROPERTY);
    pomModel.addProperty(MUNIT_PROPERTY, targetVersion("munit-maven-plugin"));
  }

  private List<Plugin> getMUnitPlugin(PomModel pomModel) {
    return pomModel.getPlugins().stream().filter(p -> StringUtils.equals(p.getArtifactId(), MUNIT_MAVEN_PLUGIN_ARTIFACT_ID))
        .collect(Collectors.toList());
  }

  private Plugin buildMunitPlugin() {
    List<PluginExecution> pluginExecutions = new ArrayList<>();
    pluginExecutions.add(new PluginExecution.PluginExecutionBuilder()
        .withId("test")
        .withGoals(newArrayList("test", "coverage-report"))
        .withPhase("test")
        .build());
    return new Plugin.PluginBuilder()
        .withGroupId(MUNIT_MAVEN_PLUGIN_GROUP_ID)
        .withArtifactId(MUNIT_MAVEN_PLUGIN_ARTIFACT_ID)
        .withVersion(targetVersion("munit-maven-plugin"))
        .withExecutions(pluginExecutions)
        .build();
  }
}
