/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PluginExecution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.Repository;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import java.util.Arrays;
import java.util.Optional;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Migrate policy deploy properties on pom.xml
 *
 * @author Mulesoft Inc.
 */
public class PolicyDeployPropertiesPomContributionMigrationStep implements PomContribution {

  private static final String GROUP_ID_VALUE = "{orgId}";

  private static final String EXCHANGE_URL_KEY = "exchange.url";
  private static final String EXCHANGE_URL_VALUE = "https://maven.anypoint.mulesoft.com/api/v1/organizations/{orgId}/maven";
  private static final String MULE_MAVEN_PLUGIN_VERSION_KEY = "mule.maven.plugin.version";
  private static final String MULE_MAVEN_PLUGIN_VERSION_VALUE = "3.2.0";

  private static final String EXCHANGE_SERVER_ID = "exchange-server";
  private static final String EXCHANGE_SERVER_NAME = "MuleSoft Exchange Environment";
  private static final String EXCHANGE_SERVER_URL = "${" + EXCHANGE_URL_KEY + "}";

  private static final String DISTRIBUTION_MANAGEMENT_REPOSITORY_NAME = "Corporate Repository";
  private static final String DISTRIBUTION_MANAGEMENT_LAYOUT = "default";

  private static final String MULE_MAVEN_PLUGIN_GROUP_ID = "org.mule.tools.maven";
  private static final String MULE_MAVEN_PLUGIN_ARTIFACT_ID = "mule-maven-plugin";
  private static final String MULE_MAVEN_PLUGIN_VERSION = "${" + MULE_MAVEN_PLUGIN_VERSION_KEY + "}";
  private static final String MULE_MAVEN_PLUGIN_EXTENSIONS = "true";

  private static final String MAVEN_DEPLOY_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
  private static final String MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID = "maven-deploy-plugin";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_ID = "upload-template";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_PHASE = "deploy";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_GOAL = "deploy-file";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_REPOSITORY_ID = EXCHANGE_SERVER_ID;
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_URL = EXCHANGE_SERVER_URL;
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_FILE = "${project.basedir}/${project.artifactId}.yaml";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_GENERATE_POM = "false";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_GROUP_ID = "${project.groupId}";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_ARTIFACT_ID = "${project.artifactId}";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_VERSION = "${project.version}";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_PACKAGING = "yaml";
  private static final String MAVEN_DEPLOY_PLUGIN_EXECUTION_CLASSIFIER = "policy-definition";

  private static final String CONFIGURATION_KEY = "configuration";
  private static final String REPOSITORY_ID_KEY = "repositoryId";
  private static final String URL_KEY = "url";
  private static final String FILE_KEY = "file";
  private static final String GENERATE_POM_KEY = "generatePom";
  private static final String GROUP_ID_KEY = "groupId";
  private static final String ARTIFACT_ID_KEY = "artifactId";
  private static final String VERSION_KEY = "version";
  private static final String PACKAGING_KEY = "packaging";
  private static final String CLASSIFIER_KEY = "classifier";

  private static final String PLUGIN_REPOSITORY_ID = "mule-plugin";
  private static final String PLUGIN_REPOSITORY_NAME = "Mule Repository";
  private static final String PLUGIN_REPOSITORY_URL = "https://repository.mulesoft.org/nexus/content/repositories/public/";

  @Override
  public String getDescription() {
    return "Pom Contribution to add properties for deploying to Exchange";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport migrationReport) throws RuntimeException {
    pomModel.setGroupId(GROUP_ID_VALUE);
    pomModel.getProperties().setProperty(EXCHANGE_URL_KEY, EXCHANGE_URL_VALUE);
    addRepository(pomModel);
    addDistributionManagement(pomModel);
    addPlugins(pomModel);
    addPluginRepository(pomModel);
  }

  private void addRepository(PomModel pomModel) {
    Repository repository = new Repository.RepositoryBuilder().withId(EXCHANGE_SERVER_ID).build();
    repository.setName(EXCHANGE_SERVER_NAME);
    repository.setUrl(EXCHANGE_SERVER_URL);
    repository.setSnapshotsEnabled(true);
    pomModel.addRepository(repository);
  }

  private void addDistributionManagement(PomModel pomModel) {
    DistributionManagement distributionManagement = pomModel.getMavenModelCopy().getDistributionManagement();
    if (distributionManagement == null) {
      distributionManagement = new DistributionManagement();
    }
    DeploymentRepository deploymentRepository = new DeploymentRepository();
    deploymentRepository.setId(EXCHANGE_SERVER_ID);
    deploymentRepository.setName(DISTRIBUTION_MANAGEMENT_REPOSITORY_NAME);
    deploymentRepository.setUrl(EXCHANGE_SERVER_URL);
    deploymentRepository.setLayout(DISTRIBUTION_MANAGEMENT_LAYOUT);
    distributionManagement.setRepository(deploymentRepository);
    pomModel.setDistributionManagement(distributionManagement);
  }

  private void addPlugins(PomModel pomModel) {
    addMuleMavenPlugin(pomModel);
    pomModel.addPlugin(getMavenDeployPlugin());
  }

  private void addMuleMavenPlugin(PomModel pomModel) {
    Optional<Plugin> muleMavenPluginOptional =
        pomModel.getPlugins().stream().filter(plugin -> plugin.getArtifactId().equals(MULE_MAVEN_PLUGIN_ARTIFACT_ID)).findFirst();
    if (!muleMavenPluginOptional.isPresent()) {
      pomModel.getProperties().setProperty(MULE_MAVEN_PLUGIN_VERSION_KEY, MULE_MAVEN_PLUGIN_VERSION_VALUE);
      pomModel.addPlugin(new Plugin.PluginBuilder()
          .withGroupId(MULE_MAVEN_PLUGIN_GROUP_ID)
          .withArtifactId(MULE_MAVEN_PLUGIN_ARTIFACT_ID)
          .withVersion(MULE_MAVEN_PLUGIN_VERSION)
          .withExtensions(MULE_MAVEN_PLUGIN_EXTENSIONS).build());
    }
  }

  private Plugin getMavenDeployPlugin() {
    Plugin plugin = new Plugin();
    plugin.setGroupId(MAVEN_DEPLOY_PLUGIN_GROUP_ID);
    plugin.setArtifactId(MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID);
    PluginExecution pluginExecution = new PluginExecution.PluginExecutionBuilder()
        .withId(MAVEN_DEPLOY_PLUGIN_EXECUTION_ID)
        .withPhase(MAVEN_DEPLOY_PLUGIN_EXECUTION_PHASE)
        .withGoals(Arrays.asList(MAVEN_DEPLOY_PLUGIN_EXECUTION_GOAL))
        .build();
    pluginExecution.setConfiguration(getConfigurationElement());
    plugin.setExecutions(Arrays.asList(pluginExecution));
    return plugin;
  }

  private Xpp3Dom getConfigurationElement() {
    Xpp3Dom configuration = new Xpp3Dom(CONFIGURATION_KEY);
    configuration.addChild(getElement(REPOSITORY_ID_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_REPOSITORY_ID));
    configuration.addChild(getElement(URL_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_URL));
    configuration.addChild(getElement(FILE_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_FILE));
    configuration.addChild(getElement(GENERATE_POM_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_GENERATE_POM));
    configuration.addChild(getElement(GROUP_ID_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_GROUP_ID));
    configuration.addChild(getElement(ARTIFACT_ID_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_ARTIFACT_ID));
    configuration.addChild(getElement(VERSION_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_VERSION));
    configuration.addChild(getElement(PACKAGING_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_PACKAGING));
    configuration.addChild(getElement(CLASSIFIER_KEY, MAVEN_DEPLOY_PLUGIN_EXECUTION_CLASSIFIER));
    return configuration;
  }

  private Xpp3Dom getElement(String key, String value) {
    Xpp3Dom element = new Xpp3Dom(key);
    element.setValue(value);
    return element;
  }

  private void addPluginRepository(PomModel pomModel) {
    Repository pluginRepository = new Repository.RepositoryBuilder().withId(PLUGIN_REPOSITORY_ID).build();
    pluginRepository.setName(PLUGIN_REPOSITORY_NAME);
    pluginRepository.setUrl(PLUGIN_REPOSITORY_URL);
    pomModel.addPluginRepository(pluginRepository);
  }
}
