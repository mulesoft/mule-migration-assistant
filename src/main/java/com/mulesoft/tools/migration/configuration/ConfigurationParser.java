/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.configuration;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.listFiles;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mulesoft.tools.migration.engine.builder.TaskBuilder;
import com.mulesoft.tools.migration.engine.MigrationTask;

/**
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ConfigurationParser {

  private static final String TASK_FIELD = "tasks";

  private Path configurationPath;

  public ConfigurationParser(Path configurationPath) {
    checkArgument(configurationPath != null, "The configuration file path must not be null");
    checkArgument(configurationPath.toFile().exists() == true, "The configuration file path must exists");
    this.configurationPath = configurationPath;
  }

  public List<MigrationTask> parse() {
    List<Path> paths;
    if (configurationPath.toFile().isFile()) {
      paths = singletonList(configurationPath);
    } else {
      paths = listFiles(configurationPath.toFile(), new String[] {"json"}, true).stream().map(f -> f.toPath())
          .collect(Collectors.toList());
    }

    List<MigrationTask> migrationTasks = new ArrayList<>();
    for (Path path : paths) {
      migrationTasks.addAll(parseConfigurationFile(path));
    }
    return migrationTasks;
  }

  private List<MigrationTask> parseConfigurationFile(Path configurationFilePath) {
    try {
      // TODO this should change for GSon too after we re define the model
      JSONParser parser = new JSONParser();
      Object obj = parser.parse(new FileReader(configurationFilePath.toFile()));
      JSONObject jsonObject = (JSONObject) obj;

      JSONArray tasks = (JSONArray) jsonObject.get(TASK_FIELD);

      List<MigrationTask> migrationTasks = new ArrayList<>();

      for (Object task : tasks) {
        JSONObject taskObj = (JSONObject) task;
        migrationTasks.add(TaskBuilder.build(taskObj));
      }
      return migrationTasks;

    } catch (Exception e) {
      // TODO Change for a more specific exception
      throw new RuntimeException("Failed to parse Configuration file " + this.configurationPath + ". " + e.getMessage());
    }
  }
}
