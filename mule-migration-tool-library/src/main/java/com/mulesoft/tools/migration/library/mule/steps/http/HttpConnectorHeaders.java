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
public class HttpConnectorHeaders extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http";

  public static final String XPATH_SELECTOR = "//http:*[local-name()='header' or local-name()='headers']";

  @Override
  public String getDescription() {
    return "Update HTTP headers in request/response builders.";
  }

  public HttpConnectorHeaders() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    int idx = object.getParent().indexOf(object);

    if ("headers".equals(object.getName())) {
      String headersExpr = object.getAttributeValue("expression");

      setMule4HeadersTagText(idx, object.getParentElement(), httpNamespace, report,
                             () -> getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(headersExpr)),
                             expr -> getExpressionMigrator()
                                 .wrap(getExpressionMigrator().unwrap(expr) + " ++ "
                                     + getExpressionMigrator().unwrap(getExpressionMigrator()
                                         .migrateExpression(getExpressionMigrator().wrap(headersExpr)))));

      object.getParent().removeContent(object);
      object.setText(getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(headersExpr)));
    }
    if ("header".equals(object.getName())) {
      String headerName = object.getAttributeValue("headerName");
      String headerValue = object.getAttributeValue("value");

      String dwHeaderMapElement = "'" + headerName + "' : "
          + (getExpressionMigrator().isWrapped(headerValue)
              ? getExpressionMigrator().unwrap(getExpressionMigrator().migrateExpression(headerValue))
              : ("'" + headerValue + "'"));

      setMule4HeadersTagText(idx, object.getParentElement(), httpNamespace, report,
                             () -> getExpressionMigrator().wrap("{" + dwHeaderMapElement + "}"),
                             expr -> getExpressionMigrator()
                                 .wrap(getExpressionMigrator().unwrap(expr) + " ++ {" + dwHeaderMapElement + "}"));

      object.getParent().removeContent(object);
    }
  }

  private void setMule4HeadersTagText(int idx, Element parentTag, Namespace httpNamespace, MigrationReport report,
                                      Supplier<String> headersExprCreate,
                                      Function<String, String> headersExprAppend) {
    final Element mule4HeadersTag = lookupMule4HeadersTag(idx, parentTag, httpNamespace, report);
    mule4HeadersTag.setText(getExpressionMigrator().wrap(StringUtils.isEmpty(mule4HeadersTag.getText())
        ? headersExprCreate.get()
        : headersExprAppend.apply(mule4HeadersTag.getText())));

  }

  private Element lookupMule4HeadersTag(int idx, Element parentTag, Namespace httpNamespace, MigrationReport report) {
    final List<Element> children = parentTag.getChildren("headers", httpNamespace);

    return children.stream().filter(c -> StringUtils.isNotEmpty(c.getTextTrim())).findAny()
        .orElseGet(() -> {
          final Element headers = new Element("headers", httpNamespace);

          report.report(WARN, headers, parentTag,
                        "Build the headers map with a single DW expression",
                        "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
          parentTag.addContent(idx, headers);

          return headers;
        });
  }
}
