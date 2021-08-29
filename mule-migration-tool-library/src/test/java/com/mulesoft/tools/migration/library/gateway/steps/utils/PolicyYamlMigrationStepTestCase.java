/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.library.gateway.steps.policy.utils.PolicyYamlMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class PolicyYamlMigrationStepTestCase {

  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/file-system/");
  private static final Path YAML_PATH = Paths.get("src/test/resources/mule/apps/gateway/file-system/yaml");

  private static final List<String> DEFAULT_PROPERTIES_TO_REMOVE =
      Arrays.asList("id", "name", "description", "category", "type", "resourceLevelSupported", "standalone",
                    "requiredCharacteristics", "providedCharacteristics", "configuration");

  private MigrationReport reportMock = mock(MigrationReport.class);

  private ApplicationModel appModel;
  private Path yamlDirectory;

  @Before
  public void setup() throws Exception {
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("src/main/mule/simple-test-policy.xml")));
    amb.withProjectBasePath(YAML_PATH);
    appModel = amb.build();
  }

  @Test
  public void yamlWithResourceLevelSupported() throws Exception {
    yamlDirectory = YAML_PATH.resolve(Paths.get("valid-yaml-with-resource-level-supported"));
    String targetYamlFile = yamlDirectory.resolve("simple-test-policy-with-resource-level-supported.yaml").toString();
    PolicyYamlMigrationStep step = new PolicyYamlMigrationStep();
    step.setApplicationModel(appModel);

    Map<String, Object> yamlData = getYamlData(targetYamlFile);
    assertYamlSimpleProperties(yamlData);
    assertThat(yamlData.get("resourceLevelSupported"), is(true));

    step.execute(yamlDirectory, reportMock);

    yamlData = getYamlData(targetYamlFile);
    assertYamlSimpleProperties(yamlData);
    assertThat(yamlData.get("resourceLevelSupported"), is(true));
  }

  @Test
  public void yamlWithNoMandatoryProperties() throws Exception {
    yamlDirectory = YAML_PATH.resolve(Paths.get("valid-yaml"));
    String targetYamlFile = yamlDirectory.resolve("simple-test-policy.yaml").toString();
    PolicyYamlMigrationStep step = new PolicyYamlMigrationStep();
    step.setApplicationModel(appModel);

    Map<String, Object> yamlData = getYamlData(targetYamlFile);
    assertThat(yamlData.get("violationCategory"), is("authentication"));
    assertThat(yamlData.get("resourceLevelSupported"), nullValue());

    step.execute(yamlDirectory, reportMock);

    yamlData = getYamlData(targetYamlFile);
    assertThat(yamlData.get("violationCategory"), is("authentication"));
    assertAutocompletedYamlProperties(yamlData);
    verify(reportMock).report("basicStructure.defaultPolicyYamlValue", null, null, "resourceLevelSupported", "false");
    removeAutocompletedProperties(targetYamlFile);
  }

  private void assertAutocompletedYamlProperties(Map<String, Object> yamlData) {
    assertThat(yamlData.get("id"), is("simple-test-policy.yaml"));
    assertThat(yamlData.get("name"), is("simple-test-policy.yaml"));
    assertThat(yamlData.get("description"), is("simple-test-policy.yaml"));
    assertThat(yamlData.get("category"), is("Compliance"));
    assertThat(yamlData.get("type"), is("system"));
    assertThat(yamlData.get("resourceLevelSupported"), is(false));
    assertThat(yamlData.get("standalone"), is(true));
    assertThat(yamlData.get("requiredCharacteristics"), is(new ArrayList<>()));
    assertThat(yamlData.get("providedCharacteristics"), is(new ArrayList<>()));
    assertThat(yamlData.get("configuration"), is(new ArrayList<>()));
  }

  @Test
  public void multipleYamls() {
    yamlDirectory = YAML_PATH.resolve(Paths.get("multiple-yamls"));
    PolicyYamlMigrationStep step = new PolicyYamlMigrationStep();
    step.setApplicationModel(appModel);

    step.execute(yamlDirectory, reportMock);

    verify(reportMock).report("basicStructure.multipleYamlsFound", null, null);
  }

  @Test
  public void noYaml() {
    yamlDirectory = YAML_PATH.resolve(Paths.get("no-yaml"));
    PolicyYamlMigrationStep step = new PolicyYamlMigrationStep();
    step.setApplicationModel(appModel);

    step.execute(yamlDirectory, reportMock);

    verify(reportMock).report("basicStructure.noYamlFound", null, null);
  }

  @Test
  public void yamlWithIncompleteConfigurationItem() throws IOException {
    yamlDirectory = YAML_PATH.resolve(Paths.get("invalid-yamls"));
    String targetYamlFile = yamlDirectory.resolve("policy-with-incomplete-configuration.yaml").toString();
    PolicyYamlMigrationStep step = new PolicyYamlMigrationStep();
    step.setApplicationModel(appModel);

    Map<String, Object> yamlData = getYamlData(targetYamlFile);
    assertThat(yamlData.get("id"), is("simple-test-policy-with-incomplete-configuration"));
    List<LinkedHashMap<String, Object>> configuration = (List<LinkedHashMap<String, Object>>) yamlData.get("configuration");
    assertThat(configuration.size(), is(1));
    LinkedHashMap<String, Object> configElement = configuration.get(0);
    assertThat(configElement.get("name"), is("Configuration property"));
    assertThat(configElement.get("propertyName"), nullValue());
    assertThat(configElement.get("type"), nullValue());

    step.execute(yamlDirectory, reportMock);

    yamlData = getYamlData(targetYamlFile);
    assertThat(yamlData.get("id"), is("simple-test-policy-with-incomplete-configuration"));
    configuration = (List<LinkedHashMap<String, Object>>) yamlData.get("configuration");
    assertThat(configuration.size(), is(1));
    configElement = configuration.get(0);
    assertThat(configElement.get("name"), is("Configuration property"));
    assertThat(configElement.get("propertyName"), is("propertyName"));
    assertThat(configElement.get("type"), is("string"));
    verify(reportMock).report("basicStructure.defaultPolicyConfigurationYamlValue", null, null, "propertyName", "propertyName");
    verify(reportMock).report("basicStructure.defaultPolicyConfigurationYamlValue", null, null, "type", "string");
    removeConfigurationProperties(targetYamlFile);
  }

  private Map<String, Object> getYamlData(String yamlFile) throws FileNotFoundException {
    Yaml yaml = new Yaml();
    Map<String, Object> yamlData = new LinkedHashMap<>();
    yamlData.putAll(yaml.loadAs(new FileInputStream(yamlFile), Map.class));
    return yamlData;
  }

  private void assertYamlSimpleProperties(Map<String, Object> yamlData) {
    assertThat(yamlData.get("name"), is("Policy"));
    assertThat(yamlData.get("description"), is("Description of a simple policy"));
  }

  public void removeAutocompletedProperties(String yamlFile) throws IOException {
    Yaml yaml = new Yaml();
    Map<String, Object> yamlData = new LinkedHashMap<>();
    yamlData.putAll(yaml.loadAs(new FileInputStream(yamlFile), Map.class));
    DEFAULT_PROPERTIES_TO_REMOVE.forEach(yamlData::remove);
    yaml.dump(yamlData, new FileWriter(yamlFile));
  }

  public void removeConfigurationProperties(String yamlFile) throws IOException {
    Yaml yaml = new Yaml();
    Map<String, Object> yamlData = new LinkedHashMap<>();
    yamlData.putAll(yaml.loadAs(new FileInputStream(yamlFile), Map.class));
    ((List<LinkedHashMap<String, Object>>) yamlData.get("configuration")).get(0).remove("propertyName");
    ((List<LinkedHashMap<String, Object>>) yamlData.get("configuration")).get(0).remove("type");
    yaml.dump(yamlData, new FileWriter(yamlFile));
  }

}
