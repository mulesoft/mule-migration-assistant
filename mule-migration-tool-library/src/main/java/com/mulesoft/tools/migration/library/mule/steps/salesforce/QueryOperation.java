/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.CDATA;
import org.jdom2.Element;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Migrate Query operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QueryOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String name = "query";

  public QueryOperation() {
    super(name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    super.execute(mule3Operation, report);
    resolveAttributes(mule3Operation, mule4Operation);

    XmlDslUtils.addElementAfter(mule4Operation, mule3Operation);
    mule3Operation.getParentElement().removeContent(mule3Operation);
  }

  private void resolveAttributes(Element mule3Operation, Element mule4Operation) {
    String query = mule3Operation.getAttributeValue("query");
    if (query != null) {
      Element mule4Query = new Element("salesforce-query", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      String expression = expressionMigrator.migrateExpression(query, true, mule3Operation);
      mule4Query.setContent(new CDATA(expression));

      mule4Operation.addContent(mule4Query);
    }
  }

  @Override
  protected void migrateHeaders(Element mule3Operation, Element mule4Operation, MigrationReport report) {
    Element mule3Headers = mule3Operation.getChild("headers", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE);
    String fetchSize = mule3Operation.getAttributeValue("fetchSize");
    report.report("salesforce.fetchSize", mule3Operation, mule4Operation);
    if (mule3Headers != null) {
      String refHeaders = mule3Headers.getAttributeValue("ref");

      if (refHeaders != null) {
        String expression = expressionMigrator.migrateExpression(refHeaders, true, mule3Headers);

        String unwrappedExpression = expressionMigrator.unwrap(expression);
        if (fetchSize != null) {
          expression = expressionMigrator.wrap(unwrappedExpression + " ++ {'batchSize':\"" + fetchSize + "\"}");
        } else {
          expression = expressionMigrator.wrap(unwrappedExpression + " ++ {'batchSize':\"2000\"}");
        }

        mule4Operation.setAttribute("headers", expression);
      }

      List<Element> children = mule3Headers.getChildren();
      if (children.size() > 0) {
        Element mule4Headers = new Element("headers", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        children.stream()
            .forEach(header -> {
              mule4Headers.addContent(
                                      new Element("header", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE)
                                          .setAttribute("key", header.getAttributeValue("key"))
                                          .setAttribute("value", header.getText()));
            });
        mule4Operation.addContent(mule4Headers);

        if (fetchSize != null) {
          mule4Headers.addContent(new Element("header", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE)
              .setAttribute("key", "batchSize")
              .setAttribute("value", fetchSize));
        } else {
          mule4Headers.addContent(new Element("header", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE)
              .setAttribute("key", "batchSize")
              .setAttribute("value", "2000"));
        }
      }
    } else {
      if (fetchSize != null) {
        String expression = expressionMigrator.wrap("{'batchSize':\"" + fetchSize + "\"}");
        mule4Operation.setAttribute("headers", expression);
      } else {
        String expression = expressionMigrator.wrap("{'batchSize':\"2000\"}");
        mule4Operation.setAttribute("headers", expression);
      }
    }
  }
}
