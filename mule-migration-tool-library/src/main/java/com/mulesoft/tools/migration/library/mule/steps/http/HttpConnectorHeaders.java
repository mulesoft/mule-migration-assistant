/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorHeaders extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + HTTP_NAMESPACE_URI
          + "' and (local-name()='header' or (local-name()='headers' and normalize-space(text())='' and @expression))]";

  @Override
  public String getDescription() {
    return "Update HTTP headers in request/response builders.";
  }

  public HttpConnectorHeaders() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(HTTP_NAMESPACE);

    int idx = object.getParent().indexOf(object);

    if ("headers".equals(object.getName()) && StringUtils.isEmpty(object.getText())
        && object.getAttributeValue("expression") != null) {
      String headersExpr = object.getAttributeValue("expression");

      setMule4MapBuilderTagText(idx, "headers", object.getParentElement(), HTTP_NAMESPACE, report,
                                () -> getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(headersExpr), true,
                                                                                object),
                                expr -> getExpressionMigrator()
                                    .wrap(getExpressionMigrator().unwrap(expr) + " ++ "
                                        + getExpressionMigrator().unwrap(getExpressionMigrator()
                                            .migrateExpression(getExpressionMigrator().wrap(headersExpr), true, object))));


      object.getParent().removeContent(object);
      setText(object, getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(headersExpr), true, object));
    }
    if ("header".equals(object.getName())) {
      String headerName = object.getAttributeValue("headerName");
      String headerValue = object.getAttributeValue("value");

      String migratedName = getExpressionMigrator().migrateExpression(headerName, true, object.getParentElement());
      String migratedValue = getExpressionMigrator().migrateExpression(headerValue, true, object);

      String dwHeaderMapElement = migrateToDwMapKey(migratedName)
          + " : " + toExpressionOrToLiteral(migratedValue);

      setMule4MapBuilderTagText(idx, "headers", object.getParentElement(), HTTP_NAMESPACE, report,
                                () -> getExpressionMigrator().wrap("{" + dwHeaderMapElement + "}"),
                                expr -> getExpressionMigrator()
                                    .wrap(getExpressionMigrator().unwrap(expr) + " ++ {" + dwHeaderMapElement + "}"));

      object.getParent().removeContent(object);
    }
  }

  private String toExpressionOrToLiteral(String value) {
    return (getExpressionMigrator().isWrapped(value)
        ? getExpressionMigrator().unwrap(value)
        : "'" + value + "'");
  }

  public String migrateToDwMapKey(String originalExpression) {
    return getExpressionMigrator().isWrapped(originalExpression)
        ? "(" + getExpressionMigrator().unwrap(originalExpression) + ")"
        : "'" + originalExpression + "'";
  }

}
