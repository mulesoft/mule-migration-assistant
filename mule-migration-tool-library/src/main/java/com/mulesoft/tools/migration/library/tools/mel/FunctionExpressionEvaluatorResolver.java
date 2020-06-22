/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Resolver for function expressions
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FunctionExpressionEvaluatorResolver implements CompatibilityResolver<String> {

  private static final String NOW_FUNCTION = "now";
  private static final String DATE_FUNCTION = "date";
  private static final String DATESTAMP_FUNCTION = "datestamp";
  private static final String SYSTIME_FUNCTION = "systime";
  private static final String UUID_FUNCTION = "uuid";
  private static final String HOSTNAME_FUNCTION = "hostname";
  private static final String IP_FUNCTION = "ip";
  private static final String PAYLOAD_CLASS_FUNCTION = "payloadClass";
  private static final String SHORT_PAYLOAD_CLASS_FUNCTION = "shortPayloadClass";
  private static final String DEFAULT_DATESTAMP_FORMAT = "dd-MM-yy_HH-mm-ss.SSS";

  @Override
  public boolean canResolve(String original) {
    return original != null && original.trim().toLowerCase().startsWith("function:");
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    String functionName = getFunctionName(original);

    if (NOW_FUNCTION.equalsIgnoreCase(functionName) || DATE_FUNCTION.equalsIgnoreCase(functionName)) {
      return "now()";
    } else if (StringUtils.startsWithIgnoreCase(functionName, DATESTAMP_FUNCTION)) {
      String temp = functionName.substring(DATESTAMP_FUNCTION.length());
      String format = temp.length() == 0 ? DEFAULT_DATESTAMP_FORMAT : "${" + temp.substring(1) + "}";
      return "now() as String {format: \"" + format + "\"}";
    } else if (UUID_FUNCTION.equalsIgnoreCase(functionName)) {
      return "uuid()";
    } else if (SYSTIME_FUNCTION.equalsIgnoreCase(functionName)) {
      return "now() as Number {unit: \"milliseconds\"}";
    } else if (HOSTNAME_FUNCTION.equalsIgnoreCase(functionName)) {
      return "server.host";
    } else if (IP_FUNCTION.equalsIgnoreCase(functionName)) {
      return "server.ip";
    } else if (PAYLOAD_CLASS_FUNCTION.equalsIgnoreCase(functionName)) {
      return "payload.^class";
    } else if (SHORT_PAYLOAD_CLASS_FUNCTION.equalsIgnoreCase(functionName)) {
      return "( payload.^class splitBy  '.' )[-1]";
    }

    return "mel:" + original;

  }

  protected String getFunctionName(String original) {
    return original.trim().replaceFirst("(?i)^function:", EMPTY);
  }
}
