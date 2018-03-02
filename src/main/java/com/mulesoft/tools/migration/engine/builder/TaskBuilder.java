/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.builder;

import com.google.gson.Gson;
import com.mulesoft.tools.migration.engine.task.DefaultMigrationTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * It knows how to build a {@link DefaultMigrationTask}
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class TaskBuilder {

  public static final String STEPS_FIELD = "stepsDefinition";

  public static DefaultMigrationTask build(JSONObject taskDef) throws Exception {

    DefaultMigrationTask migrationTask = new Gson().fromJson(taskDef.toJSONString(), DefaultMigrationTask.class);

    JSONArray steps = (JSONArray) taskDef.get(STEPS_FIELD);

    for (Object step : steps) {
      JSONObject stepObj = (JSONObject) step;
      migrationTask.addStep(StepBuilder.build(stepObj));
    }

    return migrationTask;
  }
}
