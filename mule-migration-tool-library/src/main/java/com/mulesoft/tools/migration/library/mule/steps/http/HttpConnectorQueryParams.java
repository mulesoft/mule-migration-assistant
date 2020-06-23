/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorQueryParams extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + HTTP_NAMESPACE_URI
          + "' and (local-name()='query-param' or (local-name()='query-params' and normalize-space(text())=''))]";

  @Override
  public String getDescription() {
    return "Update HTTP query params in request builders.";
  }

  public HttpConnectorQueryParams() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(HTTP_NAMESPACE);

    int idx = object.getParent().indexOf(object);

    if ("query-params".equals(object.getName())) {
      String paramsExpr = object.getAttributeValue("expression");

      setMule4MapBuilderTagText(idx, "query-params", object.getParentElement(), HTTP_NAMESPACE, report,
                                () -> getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(paramsExpr), true,
                                                                                object),
                                expr -> getExpressionMigrator()
                                    .wrap(getExpressionMigrator().unwrap(expr) + " ++ "
                                        + getExpressionMigrator().unwrap(getExpressionMigrator()
                                            .migrateExpression(getExpressionMigrator().wrap(paramsExpr), true, object))));

      object.getParent().removeContent(object);
      setText(object, getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(paramsExpr), true, object));
    }
    if ("query-param".equals(object.getName())) {
      String paramName = object.getAttributeValue("paramName");
      String paramValue = object.getAttributeValue("value");

      String dwParamMapElement = migrateToDwMapKey(paramName, object) + " : "
          + (getExpressionMigrator().isWrapped(paramValue)
              ? getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(paramValue, true, object))
              : ("'" + paramValue + "'"));

      setMule4MapBuilderTagText(idx, "query-params", object.getParentElement(), HTTP_NAMESPACE, report,
                                () -> getExpressionMigrator().wrap("{" + dwParamMapElement + "}"),
                                expr -> getExpressionMigrator()
                                    .wrap(getExpressionMigrator().unwrap(expr) + " ++ {" + dwParamMapElement + "}"));

      object.getParent().removeContent(object);
    }
  }

  public String migrateToDwMapKey(String originalExpression, Element object) {
    return (getExpressionMigrator().isWrapped(originalExpression)
        ? "(" + getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(originalExpression, true, object)) + ")"
        : ("'" + originalExpression + "'"));
  }

}
