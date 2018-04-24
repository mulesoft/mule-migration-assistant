/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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

  protected static final String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http";
  protected static final String TLS_NAMESPACE = "http://www.mulesoft.org/schema/mule/tls";
  private ExpressionMigrator expressionMigrator;

  protected void setMule4MapBuilderTagText(int idx, String tagName, Element parentTag, Namespace httpNamespace,
                                           MigrationReport report, Supplier<String> paramsExprCreate,
                                           Function<String, String> paramsExprAppend) {
    final Element mule4MapBuilderTag = lookupMule4MapBuilderTag(idx, tagName, parentTag, httpNamespace, report);
    mule4MapBuilderTag.setText(getExpressionMigrator().wrap(StringUtils.isEmpty(mule4MapBuilderTag.getText())
        ? paramsExprCreate.get()
        : paramsExprAppend.apply(mule4MapBuilderTag.getText())));

  }

  private Element lookupMule4MapBuilderTag(int idx, String tagName, Element parentTag, Namespace httpNamespace,
                                           MigrationReport report) {
    final List<Element> children = parentTag.getChildren(tagName, httpNamespace);

    return children.stream().filter(c -> StringUtils.isNotEmpty(c.getTextTrim())).findAny()
        .orElseGet(() -> {
          final Element mapBuilderElement = new Element(tagName, httpNamespace);

          report.report(WARN, mapBuilderElement, parentTag,
                        "Build the '" + tagName + "' map with a single DW expression",
                        "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
          parentTag.addContent(idx, mapBuilderElement);

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
