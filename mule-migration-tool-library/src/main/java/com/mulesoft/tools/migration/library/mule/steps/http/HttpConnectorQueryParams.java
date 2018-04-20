/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorQueryParams extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http";

  public static final String XPATH_SELECTOR = "//http:*[local-name()='query-param' or local-name()='query-params']";

  @Override
  public String getDescription() {
    return "Update HTTP query params in request builders.";
  }

  public HttpConnectorQueryParams() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    int idx = object.getParent().indexOf(object);

    if ("query-params".equals(object.getName())) {
      String paramsExpr = object.getAttributeValue("expression");

      setMule4ParamsTagText(idx, object.getParentElement(), httpNamespace, report,
                            () -> getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(paramsExpr)),
                            expr -> getExpressionMigrator()
                                .wrap(getExpressionMigrator().unwrap(expr) + " ++ "
                                    + getExpressionMigrator().unwrap(getExpressionMigrator()
                                        .migrateExpression(getExpressionMigrator().wrap(paramsExpr)))));

      object.getParent().removeContent(object);
      object.setText(getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(paramsExpr)));
    }
    if ("query-param".equals(object.getName())) {
      String paramName = object.getAttributeValue("paramName");
      String paramValue = object.getAttributeValue("value");

      String dwParamMapElement = (getExpressionMigrator().isWrapped(paramName)
          ? getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(paramName))
          : ("'" + paramName + "'")) + " : "
          + (getExpressionMigrator().isWrapped(paramValue)
              ? getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(paramValue))
              : ("'" + paramValue + "'"));

      setMule4ParamsTagText(idx, object.getParentElement(), httpNamespace, report,
                            () -> getExpressionMigrator().wrap("{" + dwParamMapElement + "}"),
                            expr -> getExpressionMigrator()
                                .wrap(getExpressionMigrator().unwrap(expr) + " ++ {" + dwParamMapElement + "}"));

      object.getParent().removeContent(object);
    }
  }

  private void setMule4ParamsTagText(int idx, Element parentTag, Namespace httpNamespace, MigrationReport report,
                                     Supplier<String> paramsExprCreate,
                                     Function<String, String> paramsExprAppend) {
    final Element mule4QueryParamsTag = lookupMule4QueryParamsTag(idx, parentTag, httpNamespace, report);
    mule4QueryParamsTag.setText(getExpressionMigrator().wrap(StringUtils.isEmpty(mule4QueryParamsTag.getText())
        ? paramsExprCreate.get()
        : paramsExprAppend.apply(mule4QueryParamsTag.getText())));

  }

  private Element lookupMule4QueryParamsTag(int idx, Element parentTag, Namespace httpNamespace, MigrationReport report) {
    final List<Element> children = parentTag.getChildren("query-params", httpNamespace);

    return children.stream().filter(c -> StringUtils.isNotEmpty(c.getTextTrim())).findAny()
        .orElseGet(() -> {
          final Element queryParams = new Element("query-params", httpNamespace);

          report.report(WARN, queryParams, parentTag,
                        "Build the query-params map with a single DW expression",
                        "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
          parentTag.addContent(idx, queryParams);

          return queryParams;
        });
  }
}
