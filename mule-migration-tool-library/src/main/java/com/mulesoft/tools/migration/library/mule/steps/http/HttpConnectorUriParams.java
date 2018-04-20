/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorUriParams extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR = "//http:*[local-name()='uri-param' or local-name()='uri-params']";

  @Override
  public String getDescription() {
    return "Update HTTP uri params in request builders.";
  }

  public HttpConnectorUriParams() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    int idx = object.getParent().indexOf(object);

    if ("uri-params".equals(object.getName())) {
      String paramsExpr = object.getAttributeValue("expression");

      setMule4MapBuilderTagText(idx, "uri-params", object.getParentElement(), httpNamespace, report,
                                () -> getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(paramsExpr), true),
                                expr -> getExpressionMigrator()
                                    .wrap(getExpressionMigrator().unwrap(expr) + " ++ "
                                        + getExpressionMigrator().unwrap(getExpressionMigrator()
                                            .migrateExpression(getExpressionMigrator().wrap(paramsExpr), true))));

      object.getParent().removeContent(object);
      object.setText(getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(paramsExpr), true));
    }
    if ("uri-param".equals(object.getName())) {
      String paramName = object.getAttributeValue("paramName");
      String paramValue = object.getAttributeValue("value");

      String dwParamMapElement = (getExpressionMigrator().isWrapped(paramName)
          ? getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(paramName, true))
          : ("'" + paramName + "'")) + " : "
          + (getExpressionMigrator().isWrapped(paramValue)
              ? getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(paramValue, true))
              : ("'" + paramValue + "'"));

      setMule4MapBuilderTagText(idx, "uri-params", object.getParentElement(), httpNamespace, report,
                                () -> getExpressionMigrator().wrap("{" + dwParamMapElement + "}"),
                                expr -> getExpressionMigrator()
                                    .wrap(getExpressionMigrator().unwrap(expr) + " ++ {" + dwParamMapElement + "}"));

      object.getParent().removeContent(object);
    }
  }

}
