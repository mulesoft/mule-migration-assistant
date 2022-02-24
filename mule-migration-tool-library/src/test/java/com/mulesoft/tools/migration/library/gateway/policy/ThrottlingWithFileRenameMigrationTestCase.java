/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.policy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.ARTIFACT_ID_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.CLASSIFIER_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
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
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_EXTENSIONS;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_MAVEN_PLUGIN_VERSION_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_THROTTLING_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PACKAGING_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PLUGIN_REPOSITORY_NAME;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.PLUGIN_REPOSITORY_URL;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.REPOSITORY_ID_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.SRC_MAIN_MULE_PATH;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.TEMPLATE_XML;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.URL_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.VERSION_KEY;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.XML_EXTENSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.YAML_EXTENSION;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.BasicStructureMigrationTask;
import com.mulesoft.tools.migration.library.gateway.tasks.PolicyUtilsMigrationTask;
import com.mulesoft.tools.migration.library.gateway.tasks.ThrottlingMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PluginExecution;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.Repository;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.DeploymentRepository;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.yaml.snakeyaml.Yaml;

@RunWith(Parameterized.class)
public class ThrottlingWithFileRenameMigrationTestCase {

  private static final Path PROJECT_EXAMPLES_PATH = Paths.get("mule/apps/gateway/full-policy-migration-file-system");
  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources").resolve(PROJECT_EXAMPLES_PATH);
  private static final Path POLICIES_FULL_PATH = APPLICATION_MODEL_PATH.resolve(SRC_MAIN_MULE_PATH);

  private static final String POLICY_YAML = "rate-limiting" + YAML_EXTENSION;

  private final Path configPath;
  private final String configFilename;
  private final Path targetPath;
  private final Path pomPath;
  private final MigrationReport reportMock;
  private ApplicationModel appModel;
  private Document doc;

  private List<MigrationStep> steps;

  @Parameterized.Parameters(name = "{0}: {0}, {1}, {2}")
  public static Collection<Object[]> params() {
    return asList(new Object[][] {
        {"rate-limiting", "rate-limiting4.xml", "pom.xml"},
    });
  }

  public ThrottlingWithFileRenameMigrationTestCase(final String original, final String target, final String pom) {
    configFilename = original;
    configPath = PROJECT_EXAMPLES_PATH.resolve(SRC_MAIN_MULE_PATH).resolve(original + XML_EXTENSION);
    targetPath = PROJECT_EXAMPLES_PATH.resolve(target);
    pomPath = APPLICATION_MODEL_PATH.resolve(pom);
    reportMock = mock(MigrationReport.class);
  }

  @Before
  public void setUp() throws Exception {
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withConfigurationFiles(Arrays.asList(POLICIES_FULL_PATH.resolve(configFilename + XML_EXTENSION)));
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    amb.withPom(pomPath);
    appModel = amb.build();

    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    ThrottlingMigrationTask throttlingMigrationTask = new ThrottlingMigrationTask();
    throttlingMigrationTask.setApplicationModel(appModel);
    steps = throttlingMigrationTask.getSteps();
    BasicStructureMigrationTask basicStructureTask = new BasicStructureMigrationTask();
    basicStructureTask.setApplicationModel(appModel);
    steps.addAll(basicStructureTask.getSteps());
    steps.addAll(new PolicyUtilsMigrationTask().getSteps());
  }

  private void migrate(MigrationStep migrationStep) {
    if (migrationStep instanceof AbstractApplicationModelMigrationStep) {
      getElementsFromDocument(doc, ((AbstractApplicationModelMigrationStep) migrationStep).getAppliedTo().getExpression())
          .forEach(node -> migrationStep.execute(node, reportMock));
    } else if (migrationStep instanceof ProjectStructureContribution) {
      migrationStep.execute(appModel.getProjectBasePath(), reportMock);
    } else if (migrationStep instanceof PomContribution) {
      migrationStep.execute(appModel.getPomModel().get(), reportMock);
    }
  }

  private void assertFileRename() {
    assertThat(APPLICATION_MODEL_PATH.resolve(POLICY_YAML).toFile().exists(), is(true));
    assertThat(POLICIES_FULL_PATH.resolve(TEMPLATE_XML).toFile().exists(), is(true));
    assertThat(POLICIES_FULL_PATH.resolve(configFilename + XML_EXTENSION).toFile().exists(), is(false));
    assertThat(appModel.getApplicationDocuments().size(), is(1));
    assertThat(appModel.getApplicationDocuments().get(SRC_MAIN_MULE_PATH.resolve(TEMPLATE_XML)), notNullValue());
    assertThat(appModel.getApplicationDocuments().get(SRC_MAIN_MULE_PATH.resolve(configFilename + XML_EXTENSION)), nullValue());
  }

  private void assertPomModel() {
    PomModel pomModel = appModel.getPomModel().get();
    assertThat(pomModel.getDependencies().size(), is(2));
    Dependency httpTransformDependency = pomModel.getDependencies().get(0);
    assertThat(httpTransformDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpTransformDependency.getArtifactId(), is(MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID));
    assertThat(httpTransformDependency.getVersion(), is(notNullValue()));
    assertThat(httpTransformDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
    Dependency policyTransformExtensionDependency = pomModel.getDependencies().get(1);
    assertThat(policyTransformExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(policyTransformExtensionDependency.getArtifactId(), is(MULE_THROTTLING_EXTENSION_ARTIFACT_ID));
    assertThat(policyTransformExtensionDependency.getVersion(), is(notNullValue()));
    assertThat(policyTransformExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
    assertPomDeployProperties(pomModel);
  }

  private void assertPomDeployProperties(PomModel pomModel) {
    assertThat(pomModel.getGroupId(), is(GROUP_ID_VALUE));

    Properties pomProperties = pomModel.getProperties();
    assertNotNull(pomProperties);
    assertThat(pomProperties.size(), is(5));
    assertThat(pomProperties.getProperty(EXCHANGE_URL_KEY), is(EXCHANGE_URL_VALUE));
    assertThat(pomProperties.getProperty(MULE_MAVEN_PLUGIN_VERSION_KEY), is(notNullValue()));

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

  private void assertYaml() throws FileNotFoundException {
    Yaml yaml = new Yaml();
    Map<String, Object> yamlData = new LinkedHashMap<>();
    yamlData.putAll(yaml.loadAs(new FileInputStream(APPLICATION_MODEL_PATH.resolve(POLICY_YAML).toFile()), Map.class));
    assertThat(yamlData.get("resourceLevelSupported"), notNullValue());
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate(step));

    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());

    assertPomModel();
    assertFileRename();
    assertYaml();
  }

  @After
  public void setOriginalNames() {
    APPLICATION_MODEL_PATH.resolve(POLICY_YAML).toFile()
        .renameTo(APPLICATION_MODEL_PATH.resolve(configFilename + YAML_EXTENSION).toFile());
    POLICIES_FULL_PATH.resolve(TEMPLATE_XML).toFile()
        .renameTo(POLICIES_FULL_PATH.resolve(configFilename + XML_EXTENSION).toFile());
  }

}
