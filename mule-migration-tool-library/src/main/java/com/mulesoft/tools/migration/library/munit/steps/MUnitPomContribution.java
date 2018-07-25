/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

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

import static com.google.common.collect.Lists.newArrayList;

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
  private static final String MUNIT_MAVEN_PLUGIN_VERSION = "2.1.2";

  @Override
  public String getDescription() {
    return "Add MUnit dependencies.";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.munit")
        .withArtifactId("munit-runner")
        .withVersion(MUNIT_MAVEN_PLUGIN_VERSION)
        .withClassifier("mule-plugin")
        .withScope("test")
        .build());

    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.munit")
        .withArtifactId("munit-tools")
        .withVersion(MUNIT_MAVEN_PLUGIN_VERSION)
        .withClassifier("mule-plugin")
        .withScope("test")
        .build());

    if (!getMUnitPlugin(pomModel).isEmpty()) {
      Plugin munitPlugin = getMUnitPlugin(pomModel).get(0);
      munitPlugin.setVersion(MUNIT_MAVEN_PLUGIN_VERSION);
    } else {
      pomModel.addPlugin(buildMunitPlugin());
    }

    pomModel.removeProperty(MUNIT_SUPPORT_PROPERTY);
    pomModel.removeProperty(MUNIT_PROPERTY);
    pomModel.addProperty(MUNIT_PROPERTY, MUNIT_MAVEN_PLUGIN_VERSION);
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
        .withVersion(MUNIT_MAVEN_PLUGIN_VERSION)
        .withExecutions(pluginExecutions)
        .build();
  }
}
