/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.utils;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.ARTIFACT_ID_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CLASSIFIER_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.DISTRIBUTION_MANAGEMENT_LAYOUT;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.DISTRIBUTION_MANAGEMENT_REPOSITORY_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXCHANGE_SERVER_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXCHANGE_SERVER_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXCHANGE_SERVER_URL;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXCHANGE_URL_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.EXCHANGE_URL_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.FILE_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GENERATE_POM_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GROUP_ID_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.GROUP_ID_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_FILE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_GENERATE_POM;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_GOAL;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_PACKAGING;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_PHASE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_REPOSITORY_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_URL;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_EXECUTION_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MAVEN_DEPLOY_PLUGIN_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_EXTENSIONS;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_VERSION_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_VERSION_VALUE;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PACKAGING_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PLUGIN_REPOSITORY_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PLUGIN_REPOSITORY_URL;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.REPOSITORY_ID_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.URL_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VERSION_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.PolicyDeployPropertiesPomContributionMigrationStep;
import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PluginExecution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.Repository;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;
import java.util.Properties;

import org.apache.maven.model.DeploymentRepository;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Test;

public class PolicyDeployPropertiesPomContributionMigrationStepTestCase {

  @Test
  public void pomDeployPropertiesTest() {
    PomModel pomModel = new PomModel();
    PolicyDeployPropertiesPomContributionMigrationStep step = new PolicyDeployPropertiesPomContributionMigrationStep();
    step.execute(pomModel, mock(MigrationReport.class));

    assertThat(pomModel.getGroupId(), is(GROUP_ID_VALUE));

    Properties pomProperties = pomModel.getProperties();
    assertNotNull(pomProperties);
    assertThat(pomProperties.size(), is(2));
    assertThat(pomProperties.getProperty(EXCHANGE_URL_KEY), is(EXCHANGE_URL_VALUE));
    assertThat(pomProperties.getProperty(MULE_MAVEN_PLUGIN_VERSION_KEY), is(MULE_MAVEN_PLUGIN_VERSION_VALUE));

    List<Repository> repositoryList = pomModel.getRepositories();
    assertThat(repositoryList.size(), is(1));
    Repository repository = repositoryList.get(0);
    assertThat(repository.getId(), is(EXCHANGE_SERVER_ID));
    assertThat(repository.getName(), is(EXCHANGE_SERVER_NAME));
    assertThat(repository.getUrl(), is(EXCHANGE_SERVER_URL));
    assertThat(repository.areSnapshotsEnabled(), is(true));

    DeploymentRepository deploymentRepository = pomModel.getMavenModelCopy().getDistributionManagement().getRepository();
    assertNotNull(deploymentRepository);
    assertThat(deploymentRepository.getId(), is(EXCHANGE_SERVER_ID));
    assertThat(deploymentRepository.getName(), is(DISTRIBUTION_MANAGEMENT_REPOSITORY_NAME));
    assertThat(deploymentRepository.getUrl(), is(EXCHANGE_SERVER_URL));
    assertThat(deploymentRepository.getLayout(), is(DISTRIBUTION_MANAGEMENT_LAYOUT));

    List<Plugin> pluginList = pomModel.getPlugins();
    assertThat(pluginList.size(), is(2));
    Plugin muleMavenPlugin = pluginList.get(0);
    assertThat(muleMavenPlugin.getGroupId(), is(MULE_MAVEN_PLUGIN_GROUP_ID));
    assertThat(muleMavenPlugin.getArtifactId(), is(MULE_MAVEN_PLUGIN_ARTIFACT_ID));
    assertThat(muleMavenPlugin.getVersion(), is(MULE_MAVEN_PLUGIN_VERSION));
    assertThat(muleMavenPlugin.getExtensions(), is(MULE_MAVEN_PLUGIN_EXTENSIONS));
    Plugin mavenDeployPlugin = pluginList.get(1);
    assertThat(mavenDeployPlugin.getGroupId(), is(MAVEN_DEPLOY_PLUGIN_GROUP_ID));
    assertThat(mavenDeployPlugin.getArtifactId(), is(MAVEN_DEPLOY_PLUGIN_ARTIFACT_ID));
    List<PluginExecution> pluginExecutionList = mavenDeployPlugin.getExecutions();
    assertThat(pluginExecutionList.size(), is(1));
    PluginExecution pluginExecution = pluginExecutionList.get(0);
    assertThat(pluginExecution.getId(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_ID));
    assertThat(pluginExecution.getPhase(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_PHASE));
    assertThat(pluginExecution.getGoals().get(0), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_GOAL));
    Xpp3Dom configuration = pluginExecution.getConfiguration();
    assertThat(configuration.getChild(REPOSITORY_ID_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_REPOSITORY_ID));
    assertThat(configuration.getChild(URL_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_URL));
    assertThat(configuration.getChild(FILE_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_FILE));
    assertThat(configuration.getChild(GENERATE_POM_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_GENERATE_POM));
    assertThat(configuration.getChild(GROUP_ID_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_GROUP_ID));
    assertThat(configuration.getChild(ARTIFACT_ID_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_ARTIFACT_ID));
    assertThat(configuration.getChild(VERSION_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_VERSION));
    assertThat(configuration.getChild(PACKAGING_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_PACKAGING));
    assertThat(configuration.getChild(CLASSIFIER_KEY).getValue(), is(MAVEN_DEPLOY_PLUGIN_EXECUTION_CLASSIFIER));

    List<Repository> pluginRepositoriesList = pomModel.getPluginRepositories();
    assertThat(pluginRepositoriesList.size(), is(1));
    Repository pluginRepository = pluginRepositoriesList.get(0);
    assertThat(pluginRepository.getId(), is(MULE_PLUGIN_CLASSIFIER));
    assertThat(pluginRepository.getName(), is(PLUGIN_REPOSITORY_NAME));
    assertThat(pluginRepository.getUrl(), is(PLUGIN_REPOSITORY_URL));
  }

}
