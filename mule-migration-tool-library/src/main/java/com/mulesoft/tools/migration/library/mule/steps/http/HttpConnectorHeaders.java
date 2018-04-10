/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

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

    if ("headers".equals(object.getName())) {
      String headersExpr = object.getAttributeValue("expression");

      setMule4HeadersTagText(object.getParentElement(), httpNamespace,
                             () -> getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(headersExpr)),
                             expr -> StringUtils.substring(getExpressionMigrator().unwrap(expr), 0, -1) + ", "
                                 + StringUtils.substring(getExpressionMigrator().unwrap(getExpressionMigrator()
                                     .migrateExpression(getExpressionMigrator().wrap(headersExpr))), 1));

      object.getParent().removeContent(object);
      object.setText(getExpressionMigrator().migrateExpression(getExpressionMigrator().wrap(headersExpr)));
    }
    if ("header".equals(object.getName())) {
      String headerName = object.getAttributeValue("headerName");
      String headerValue = object.getAttributeValue("value");

      String dwHeaderMapElement = "'" + headerName + "' : "
          + (getExpressionMigrator().isWrapped(headerValue)
              ? getExpressionMigrator().unwrap(headerValue)
              : ("'" + headerValue + "'"));

      setMule4HeadersTagText(object.getParentElement(), httpNamespace,
                             () -> getExpressionMigrator().wrap("{" + dwHeaderMapElement + "}"),
                             expr -> StringUtils.substring(getExpressionMigrator().unwrap(expr), 0, -1) + ", "
                                 + dwHeaderMapElement + "}");

      object.getParent().removeContent(object);
    }
  }

  private void setMule4HeadersTagText(Element parentTag, Namespace httpNamespace, Supplier<String> headersExprCreate,
                                      Function<String, String> headersExprAppend) {
    final Element mule4HeadersTag = lookupMule4HeadersTag(parentTag, httpNamespace);
    mule4HeadersTag.setText(getExpressionMigrator().wrap(StringUtils.isEmpty(mule4HeadersTag.getText())
        ? headersExprCreate.get()
        : headersExprAppend.apply(mule4HeadersTag.getText())));

  }

  private Element lookupMule4HeadersTag(Element parentTag, Namespace httpNamespace) {
    final List<Element> children = parentTag.getChildren("headers", httpNamespace);

    return children.stream().filter(c -> StringUtils.isNotEmpty(c.getTextTrim())).findAny()
        .orElseGet(() -> {
          final Element headers = new Element("headers", httpNamespace);

          parentTag.addContent(headers);

          return headers;
        });
  }

  protected void copyAttributeIfPresent(final Element source, final Element target, final String attributeName) {
    copyAttributeIfPresent(source, target, attributeName, attributeName);
  }

  protected void copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                        final String targetAttributeName) {
    if (source.getAttribute(sourceAttributeName) != null) {
      target.setAttribute(targetAttributeName, source.getAttributeValue(sourceAttributeName));
      source.removeAttribute(sourceAttributeName);
    }
  }

}
