/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.builder;

import com.google.gson.Gson;
import com.mulesoft.tools.migration.task.MigrationTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TaskBuilder {

    public static final String STEPS_FIELD = "stepsDefinition";

    public static MigrationTask build(JSONObject taskDef) throws Exception {

        MigrationTask migrationTask = new Gson().fromJson(taskDef.toJSONString(), MigrationTask.class);

        JSONArray steps = (JSONArray) taskDef.get(STEPS_FIELD);

        for (Object step : steps) {
            JSONObject stepObj = (JSONObject) step;
            migrationTask.addStep(StepBuilder.build(stepObj));
        }

        return migrationTask;
    }
}
