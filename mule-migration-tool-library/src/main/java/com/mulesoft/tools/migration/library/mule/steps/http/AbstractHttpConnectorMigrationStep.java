/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Common stuff for migrators of HTTP Connector elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractHttpConnectorMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";
  public static final Namespace HTTP_NAMESPACE = getNamespace("http", HTTP_NAMESPACE_URI);
  public static final String HTTPS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/https";
  public static final Namespace HTTPS_NAMESPACE = getNamespace("https", HTTPS_NAMESPACE_URI);
  public static final String TLS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/tls";
  protected static final String TLS_NAMESPACE_PREFIX = "tls";
  public static final Namespace TLS_NAMESPACE = getNamespace(TLS_NAMESPACE_PREFIX, TLS_NAMESPACE_URI);

  private ExpressionMigrator expressionMigrator;

  protected void setMule4MapBuilderTagText(int idx, String tagName, Element parentTag, Namespace httpNamespace,
                                           MigrationReport report, Supplier<String> paramsExprCreate,
                                           Function<String, String> paramsExprAppend) {
    final Element mule4MapBuilderTag = lookupMule4MapBuilderTag(idx, tagName, parentTag, httpNamespace, report);
    setText(mule4MapBuilderTag, getExpressionMigrator().wrap(StringUtils.isEmpty(mule4MapBuilderTag.getText())
        ? paramsExprCreate.get()
        : paramsExprAppend.apply(mule4MapBuilderTag.getText())));

  }

  private Element lookupMule4MapBuilderTag(int idx, String tagName, Element parentTag, Namespace httpNamespace,
                                           MigrationReport report) {
    final List<Element> children = parentTag.getChildren(tagName, httpNamespace);

    return children.stream().filter(c -> StringUtils.isNotEmpty(c.getTextTrim())).findAny()
        .orElseGet(() -> {
          final Element mapBuilderElement = new Element(tagName, httpNamespace);

          parentTag.addContent(idx, mapBuilderElement);
          report.report("http.mapExpression", mapBuilderElement, parentTag, tagName);

          return mapBuilderElement;
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
