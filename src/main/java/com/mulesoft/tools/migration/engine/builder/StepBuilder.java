/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.builder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mulesoft.tools.migration.engine.step.DefaultMigrationStep;

/**
 * It knows how to build as {@link DefaultMigrationStep}
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class StepBuilder {

  public static final String TYPE_FIELD = "type";
  public static final String PARAMETERS_FIELD = "parameters";
  public static final String DESCRIPTION_FIELD = "stepDescriptor";
  public static final String SET_METHOD = "set";

  public static DefaultMigrationStep build(JSONObject stepDef) throws Exception {

    String stepType = stepDef.get(TYPE_FIELD).toString();
    String stepParameters = stepDef.get(PARAMETERS_FIELD).toString();
    String description = stepDef.get(DESCRIPTION_FIELD).toString();
    String methodName;
    Method method;

    Type mapType = new TypeToken<Map<String, String>>() {}.getType();
    Map<String, String> parameters = new Gson().fromJson(stepParameters, mapType);

    try {

      Class<?> clazz = Class.forName(stepType);
      DefaultMigrationStep step = (DefaultMigrationStep) clazz.newInstance();

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
