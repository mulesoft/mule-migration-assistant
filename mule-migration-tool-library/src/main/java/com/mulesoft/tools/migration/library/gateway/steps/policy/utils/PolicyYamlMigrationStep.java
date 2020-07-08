/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.utils;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.ProjectStructureContribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Migrate policy.yaml file
 *
 * @author Mulesoft Inc.
 */
public class PolicyYamlMigrationStep implements ProjectStructureContribution {

  private static final Logger LOGGER = LoggerFactory.getLogger(PolicyYamlMigrationStep.class);

  private static final String YAML_EXTENSION = ".yaml";
  private static final String YML_EXTENSION = ".yml";

  private static final String DEFAULT_POLICY_YAML_VALUE_MESSAGE = "basicStructure.defaultPolicyYamlValue";
  private static final String DEFAULT_POLICY_CONFIGURATION_YAML_VALUE_MESSAGE =
      "basicStructure.defaultPolicyConfigurationYamlValue";

  @Override
  public String getDescription() {
    return "Adds properties to policy YAML";
  }

  private void checkYamlProperty(Map<String, Object> yamlData, String key, Object defaultValue, MigrationReport migrationReport,
                                 String reportMessage) {
    if (!yamlData.containsKey(key)) {
      yamlData.put(key, defaultValue);
      migrationReport.report(reportMessage, null, null, key, defaultValue.toString());
    }
  }

  private void checkConfigurationProperty(Map<String, Object> configurationProperty, MigrationReport migrationReport) {
    checkYamlProperty(configurationProperty, "propertyName", "propertyName", migrationReport,
                      DEFAULT_POLICY_CONFIGURATION_YAML_VALUE_MESSAGE);
    checkYamlProperty(configurationProperty, "type", "string", migrationReport, DEFAULT_POLICY_CONFIGURATION_YAML_VALUE_MESSAGE);
  }

  private void checkConfiguration(Map<String, Object> yamlData, MigrationReport migrationReport) {
    checkYamlProperty(yamlData, "configuration", new ArrayList<>(), migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    List<LinkedHashMap<String, Object>> configuration = (List<LinkedHashMap<String, Object>>) yamlData.get("configuration");
    configuration.forEach(configElement -> checkConfigurationProperty(configElement, migrationReport));
  }

  private void treatYaml(File yamlFile, MigrationReport migrationReport) throws IOException {
    Yaml yaml = new Yaml();
    Map<String, Object> yamlData = new LinkedHashMap<>();
    yamlData.putAll(yaml.loadAs(new FileInputStream(yamlFile), Map.class));
    checkYamlProperty(yamlData, "id", yamlFile.getName(), migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "name", yamlFile.getName(), migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "description", yamlFile.getName(), migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "category", "Compliance", migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "type", "system", migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "resourceLevelSupported", false, migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "standalone", true, migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "requiredCharacteristics", new ArrayList<>(), migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkYamlProperty(yamlData, "providedCharacteristics", new ArrayList<>(), migrationReport, DEFAULT_POLICY_YAML_VALUE_MESSAGE);
    checkConfiguration(yamlData, migrationReport);
    yaml.dump(yamlData, new FileWriter(yamlFile));
  }

  @Override
  public void execute(Path path, MigrationReport migrationReport) throws RuntimeException {
    File projectBasePath = path.toFile();
    if (projectBasePath.exists()) {
      File[] yamlFiles =
          projectBasePath.listFiles((FilenameFilter) new SuffixFileFilter(new String[] {YAML_EXTENSION, YML_EXTENSION}));
      if (yamlFiles.length == 1) {
        try {
          treatYaml(yamlFiles[0], migrationReport);
        } catch (IOException e) {
          migrationReport.report("basicStructure.errorEditingYaml", null, null);
          LOGGER.error("Error editing policy YAML.", e);
        }
      } else if (yamlFiles.length > 1) {
        migrationReport.report("basicStructure.multipleYamlsFound", null, null);
      } else {
        migrationReport.report("basicStructure.noYamlFound", null, null);
      }
    }
  }

}
