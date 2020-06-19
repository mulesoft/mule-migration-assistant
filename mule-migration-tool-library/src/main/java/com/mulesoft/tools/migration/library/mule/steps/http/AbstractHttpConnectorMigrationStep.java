/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
