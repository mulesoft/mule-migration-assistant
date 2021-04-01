/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.pom;

import java.util.Optional;
import java.util.Properties;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PluginExecution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds APIkit dependency
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AddSmartgateAPIPomContribution implements PomContribution {


  private static final String VERSION = "version";
  private static final String GROUP_ID = "groupId";
  private static final String UNPACK_SHARED_RESOURCES = "unpack-shared-resources";
  private static final String MAVEN_DEPENDENCY_PLUGIN = "maven-dependency-plugin";
  private static final String ARTIFACT_ITEMS = "artifactItems";
  private static final String ARTIFACT_ITEM = "artifactItem";
  private static final String ARTIFACT_ID = "artifactId";
  private String OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_VERSION = "obi.smartgate.anypoint.exchange.asset.version";
  private String OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_BUSINESSGROUPID = "obi.smartgate.anypoint.exchange.asset.businessgroupid";

  @Override
  public String getDescription() {
    return "Add Project API dependency";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {

    String tArtifactId = pomModel.getArtifactId();
    Properties properties = pomModel.getProperties();
    String tempBG = properties.getProperty(OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_BUSINESSGROUPID);
    String tempAssetVersion = properties.getProperty(OBI_SMARTGATE_ANYPOINT_EXCHANGE_ASSET_VERSION);

    Dependency dependency = new DependencyBuilder()
        .withGroupId(tempBG)
        .withArtifactId(tArtifactId)
        .withVersion(tempAssetVersion)
        .withClassifier("raml")
        .withType("zip")
        .build();
    pomModel.addDependency(dependency);


    final Optional<Plugin> tempPlugin =
        pomModel.getPlugins().stream().filter(plugin -> plugin.getArtifactId().equals(MAVEN_DEPENDENCY_PLUGIN)).findFirst();
    if (tempPlugin.isPresent()) {
      Plugin plugin = tempPlugin.get();
      final Optional<PluginExecution> execution = plugin.getExecutions().stream()
          .filter(pluginExecution -> pluginExecution.getId().equals(UNPACK_SHARED_RESOURCES)).findFirst();
      if (execution.isPresent()) {
        final PluginExecution pluginExecution = execution.get();
        final Xpp3Dom configuration = pluginExecution.getConfiguration();
        if (configuration != null) {
          final Xpp3Dom allArtifactItems = configuration.getChild(ARTIFACT_ITEMS);

          final Xpp3Dom[] artifactItem = allArtifactItems.getChildren();
          for (Xpp3Dom children : artifactItem) {
            final Xpp3Dom[] children2 = children.getChildren();
            String arifactId = null;
            String groupId = null;
            String version = null;
            String outputDirectory = null;
            for (Xpp3Dom xpp3Dom : children2) {
              String name = xpp3Dom.getName();
              if (name.equals(ARTIFACT_ID)) {
                arifactId = xpp3Dom.getValue();
              } else if (name.equals(GROUP_ID)) {
                groupId = xpp3Dom.getValue();
              } else if (name.equals(VERSION)) {
                version = xpp3Dom.getValue();
              } else if (name.equals(outputDirectory)) {
                outputDirectory = xpp3Dom.getValue();
              }
            }
            if (!"${project.artifactId}".equals(arifactId)) {
              final Dependency apiDependency = new DependencyBuilder()
                  .withGroupId(groupId)
                  .withArtifactId(arifactId)
                  .withVersion(version)
                  .withClassifier("raml")
                  .withType("zip")
                  .build();
              pomModel.addDependency(apiDependency);

            }
          }
        }
      }
    }
  }
}
