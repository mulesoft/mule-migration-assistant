/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.builder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mulesoft.tools.migration.task.step.MigrationStep;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class StepBuilder {

    public static final String TYPE_FIELD = "type";
    public static final String PARAMETERS_FIELD = "parameters";
    public static final String DESCRIPTION_FIELD = "stepDescriptor";
    public static final String SET_METHOD = "set";

    public static MigrationStep build(JSONObject stepDef) throws Exception{

        String stepType = stepDef.get(TYPE_FIELD).toString();
        String stepParameters = stepDef.get(PARAMETERS_FIELD).toString();
        String description = stepDef.get(DESCRIPTION_FIELD).toString();
        String methodName;
        Method method;

        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> parameters = new Gson().fromJson(stepParameters, mapType);

        try {

            Class<?> clazz = Class.forName(stepType);
            MigrationStep step = (MigrationStep) clazz.newInstance();

            for (String parameter : parameters.keySet()) {
                methodName = SET_METHOD + StringUtils.capitalize(parameter);
                method = clazz.getMethod(methodName, String.class);
                method.invoke(step, parameters.get(parameter));
            }

            method = clazz.getMethod(SET_METHOD + StringUtils.capitalize(DESCRIPTION_FIELD), String.class);
            method.invoke(step, description);

            return step;

        } catch (Exception ex) {
            throw new Exception("Failed to instance step: " + stepType);
        }
    }
}
