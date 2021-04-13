/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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
