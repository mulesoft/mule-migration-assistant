/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

/**
 * Migrates operations of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractDbOperationMigrator extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  protected static final String DB_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/db";
  protected final static Namespace DB_NAMESPACE = Namespace.getNamespace("db", DB_NAMESPACE_URI);

  private static final Pattern DB_3X_MEL_CONTRIB_PATTERN =
      compile("(?:dbCreateArray|dbCreateStruct)\\(['\\\"]([^'\\\"]*)['\\\"]\\s*,\\s*['\\\"]([^'\\\"]*)['\\\"]\\s*,\\s*(.*)\\)");

  private ExpressionMigrator expressionMigrator;

  protected void migrateInputParamTypes(Element object) {
    List<Element> paramTypes = object.getChildren("in-param", DB_NAMESPACE).stream()
        .filter(ip -> ip.getAttribute("type") != null)
        .map(ip -> new Element("parameter-type", DB_NAMESPACE)
            .setAttribute("key", ip.getAttributeValue("name"))
            .setAttribute("type", ip.getAttributeValue("type")))
        .collect(toList());
    if (!paramTypes.isEmpty()) {
      object.addContent(new Element("parameter-types", DB_NAMESPACE).addContent(paramTypes));
    }
  }

  protected void migrateInputParams(Element object) {
    doMigrateInputParams(object, joining(", ", "#[{", "}]"), "#[{}]", "input-parameters");
  }

  protected void migrateBulkInputParams(Element object) {
    doMigrateInputParams(object, joining(", ", "#[[{", "}]]"), "#[[{}]]", "bulk-input-parameters");
  }

  private void doMigrateInputParams(Element object, Collector<CharSequence, ?, String> inParamsJoiner, String emptyParamsExpr,
                                    String inParamsElementName) {
    Map<String, String> inputParamsMap = new LinkedHashMap<>();

    object.getChildren("in-param", DB_NAMESPACE).stream()
        .forEach(ip -> {
          // This magic string is declared in org.mule.module.db.internal.util.ValueUtils#NULL_VALUE in 3.x
          if ("NULL".equals(ip.getAttributeValue("value"))) {
            inputParamsMap.put(ip.getAttributeValue("name"), "null");
          } else {
            String originalValueExpr = ip.getAttributeValue("value");

            // The logic provided by this MEL function is already included in the connector in Mule 4
            Matcher matcher = DB_3X_MEL_CONTRIB_PATTERN.matcher(originalValueExpr);
            while (matcher.find()) {
              // these 2 params are redundant respect to the config
              String configName = matcher.group(1);
              String type = matcher.group(2);

              String value = matcher.group(3);

              int openParenthesis = 0;
              for (int i = 0; i < value.length(); ++i) {
                if ('(' == value.charAt(i)) {
                  openParenthesis++;
                }
                if (')' == value.charAt(i)) {
                  openParenthesis--;
                }

                if (openParenthesis < 0) {
                  originalValueExpr = matcher.replaceFirst(value.substring(0, i - 1));
                  break;
                }
              }
              if (openParenthesis >= 0) {
                originalValueExpr = matcher.replaceFirst(value);
              }
              matcher = DB_3X_MEL_CONTRIB_PATTERN.matcher(originalValueExpr);
            }

            String valueExpr = getExpressionMigrator().migrateExpression(originalValueExpr, true, ip);
            inputParamsMap.put(ip.getAttributeValue("name"),
                               getExpressionMigrator().isWrapped(valueExpr) ? getExpressionMigrator().unwrap(valueExpr)
                                   : "'" + valueExpr + "'");
          }
        });

    String inputParametersExpr = inputParamsMap.entrySet().stream()
        .map(entry -> format("'%s' : %s", entry.getKey(), entry.getValue()))
        .collect(inParamsJoiner);

    for (Element inParam : new ArrayList<>(object.getChildren("in-param", DB_NAMESPACE))) {
      inParam.detach();
    }
    if (!emptyParamsExpr.equals(inputParametersExpr)) {
      object.addContent(new Element(inParamsElementName, DB_NAMESPACE).setText(inputParametersExpr));
    }
  }

  protected void migrateSql(Element object) {
    object.getChildren("parameterized-query", DB_NAMESPACE).forEach(pq -> {
      pq.setName("sql");
      pq.setText(getExpressionMigrator().migrateExpression(pq.getText(), true, pq));
    });
    object.getChildren("dynamic-query", DB_NAMESPACE).forEach(dq -> {
      dq.setName("sql");
      dq.setText(getExpressionMigrator().migrateExpression(dq.getText(), true, dq));
    });
  }


  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
