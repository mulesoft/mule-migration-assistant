/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_APPLICATION_3_PACKAGING_TYPE;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_APPLICATION_4_PACKAGING_TYPE;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_VERSION;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.buildMule4MuleMavenPluginConfiguration;
import static java.util.stream.IntStream.range;

import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Update Mule Maven Plugin in pom
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateMuleMavenPlugin implements PomContribution {

  private static final String DEPLOYMENT_TYPE_PROPERTY = "deploymentType";
  private static final String DEPLOYMENT_CONFIGURATION_PROPERTY_SUFFIX = "Deployment";

  @Override
  public String getDescription() {
    return "Update mule-maven-plugin";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    String packagingType = pomModel.getPackaging();
    if (StringUtils.equals(packagingType, MULE_APPLICATION_3_PACKAGING_TYPE)) {
      pomModel.setPackaging(MULE_APPLICATION_4_PACKAGING_TYPE);
    }
    Plugin muleMavenPlugin = pomModel.removePlugin(p -> p.getArtifactId().equals(MULE_MAVEN_PLUGIN_ARTIFACT_ID))
        .orElse(buildMule4MuleMavenPluginConfiguration());
    muleMavenPlugin.setVersion(MULE_MAVEN_PLUGIN_VERSION);
    muleMavenPlugin.setExtensions("true");
    Xpp3Dom configuration = muleMavenPlugin.getConfiguration();
    if (configuration != null) {
      updateDeploymentConfiguration(configuration);
    }
    pomModel.addPlugin(muleMavenPlugin);
  }

  /**
   * Removes the copyToAppsDirectory property and updates the
   *
   * @param configuration
   */
  protected void updateDeploymentConfiguration(Xpp3Dom configuration) {
    OptionalInt deploymentTypeIndex = range(0, configuration.getChildCount())
        .filter(k -> DEPLOYMENT_TYPE_PROPERTY.equals(configuration.getChild(k).getName())).findFirst();
    if (deploymentTypeIndex.isPresent()) {
      String deploymentConfigurationPrefix = configuration.getChild(deploymentTypeIndex.getAsInt()).getValue();
      String deploymentConfigurationName = deploymentConfigurationPrefix + DEPLOYMENT_CONFIGURATION_PROPERTY_SUFFIX;
      configuration.removeChild(deploymentTypeIndex.getAsInt());
      updateConfigurationWithNewDeploymentConfiguration(deploymentConfigurationName, configuration);
    }
  }

  /**
   * Updates the configuration with a new deployment configuration. It removes all the children that are parameters used in deployment
   *
   * @param deploymentConfigurationName
   * @param configuration
   */
  protected void updateConfigurationWithNewDeploymentConfiguration(String deploymentConfigurationName, Xpp3Dom configuration) {
    Xpp3Dom deploymentConfiguration = new Xpp3Dom(deploymentConfigurationName);
    Set<String> deploymentParameters = getDeploymentParameters();
    for (String deploymentParameter : deploymentParameters) {
      moveChildWithName(deploymentParameter, configuration, deploymentConfiguration);
    }
    configuration.addChild(deploymentConfiguration);
  }

  /**
   * Move a child with {@param childName} name from the {@param from} node to the {@param to} node in case {@param from} contains a child with such name.
   *
   * @param childName the name of the child to move
   * @param from      the node that possibly contains the child
   * @param to        the node that is going to have the child appended to its child list
   */
  protected void moveChildWithName(String childName, Xpp3Dom from, Xpp3Dom to) {
    for (int k = 0; k < from.getChildren().length; ++k) {
      Xpp3Dom currentChild = from.getChild(k);
      if (currentChild.getName().equals(childName)) {
        from.removeChild(k);
        to.addChild(currentChild);
        break;
      }
    }
  }

  /**
   * Retrieves set of parameter names that are part of deployment.
   *
   * @return a set of parameter names
   */
  protected Set<String> getDeploymentParameters() {
    Set<String> deploymentParameters = new HashSet<>();
    deploymentParameters.add("deploymentTimeout");
    deploymentParameters.add("arguments");
    deploymentParameters.add("region");
    deploymentParameters.add("workers");
    deploymentParameters.add("workerType");
    deploymentParameters.add("domain");
    deploymentParameters.add("script");
    deploymentParameters.add("timeout");
    deploymentParameters.add("username");
    deploymentParameters.add("password");
    deploymentParameters.add("uri");
    deploymentParameters.add("environment");
    deploymentParameters.add("muleHome");
    deploymentParameters.add("size");
    deploymentParameters.add("businessGroup");
    deploymentParameters.add("armInsecure");
    deploymentParameters.add("target");
    deploymentParameters.add("targetType");
    return deploymentParameters;
  }


}
