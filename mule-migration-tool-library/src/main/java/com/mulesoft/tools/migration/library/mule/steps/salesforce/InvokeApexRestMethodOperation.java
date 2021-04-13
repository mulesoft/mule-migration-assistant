/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.tools.SalesforceUtils.START_TRANSFORM_BODY_TYPE_JSON;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jdom2.CDATA;
import org.jdom2.Element;

/**
 * Migrate Invoke Apex Rest Method operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InvokeApexRestMethodOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String name = "invoke-apex-rest-method";

  public InvokeApexRestMethodOperation() {
    super(name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    super.execute(mule3Operation, report);
    resolveAttributes(mule3Operation, mule4Operation);

    StringBuilder requestContents = new StringBuilder();

    String body = mule3Operation.getAttributeValue("input-ref");
    if (body != null && !body.isEmpty()) {
      String expression = expressionMigrator.migrateExpression(body, true, mule3Operation); // ?
      requestContents.append("body: " + expressionMigrator.unwrap(expression));
    }

    String headers = mule3Operation.getAttributeValue("requestHeaders-ref");
    if (headers != null && !headers.isEmpty()) {
      String expression = expressionMigrator.migrateExpression(headers, true, mule3Operation);
      if (requestContents != null && !requestContents.equals("")) {
        requestContents.append(", \n");
      }
      requestContents.append("headers: " + expressionMigrator.unwrap(expression));
    }

    Optional<Element> mule3QueryParams =
        Optional.ofNullable(mule3Operation.getChild("query-parameters", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));

    mule3QueryParams.ifPresent(queryParams -> {
      String refHeaders = queryParams.getAttributeValue("ref");
      if (refHeaders != null) {
        String expression = expressionMigrator.migrateExpression(refHeaders, true, queryParams);
        if (requestContents != null && !requestContents.equals("")) {
          requestContents.append(", \n");
        }
        requestContents.append("queryParams: " + expressionMigrator.unwrap(expression));
      }

      List<Element> children = queryParams.getChildren();

      if (children.size() > 0) {
        String queryParam = children.stream()
            .map(object -> object.getContent().stream()
                .map(innerObject -> object.getAttributeValue("key") + ": "
                    + "\"" + innerObject.getValue() + "\"")
                .collect(Collectors.joining("")))
            .collect(Collectors.joining(", "));

        if (requestContents != null && !requestContents.equals("")) {
          requestContents.append(", \n");
        }
        requestContents.append("queryParams: { " + queryParam + " }");
      }
    });

    if (requestContents != null && requestContents.length() != 0) {
      Element request = new Element("request", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      request.setContent(new CDATA(START_TRANSFORM_BODY_TYPE_JSON + requestContents.toString()
          + SalesforceUtils.CLOSE_TRANSFORM_BODY_TYPE_JSON));
      mule4Operation.addContent(request);
    }

    XmlDslUtils.addElementAfter(mule4Operation, mule3Operation);
    mule3Operation.getParentElement().removeContent(mule3Operation);
  }

  private void resolveAttributes(Element mule3Operation, Element mule4Operation) {
    String restMethodName = mule3Operation.getAttributeValue("restMethodName");
    if (restMethodName != null && !restMethodName.isEmpty()) {
      Integer index = restMethodName.indexOf("||");
      mule4Operation.setAttribute("className", restMethodName.substring(0, index));
      mule4Operation.setAttribute("methodName", restMethodName.substring(index + 2, restMethodName.length()));
    }
  }
}
