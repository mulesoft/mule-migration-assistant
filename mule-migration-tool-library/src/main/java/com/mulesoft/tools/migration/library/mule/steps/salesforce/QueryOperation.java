/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceConstants;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
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
public class QueryOperation extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String name = "query";

  private ExpressionMigrator expressionMigrator;

  public QueryOperation() {
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3QueryOperation, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(SalesforceConstants.MULE4_SALESFORCE_NAMESPACE,
                                       SalesforceConstants.MULE4_SALESFORCE_NAMESPACE_URI, mule3QueryOperation.getDocument());

    Element mule4QueryOperation = new Element(name, SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
    setAttributes(mule3QueryOperation, mule4QueryOperation);

    if (mule3QueryOperation.getAttribute("accessTokenId") != null) {
      report.report("salesforce.accessTokenId", mule3QueryOperation, mule4QueryOperation);
    }

    String query = mule3QueryOperation.getAttributeValue("query");
    if (query != null) {
      Element mule4Query = new Element("salesforce-query", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
      String expression = expressionMigrator.migrateExpression(query, true, mule3QueryOperation);
      mule4Query.setContent(new CDATA(expression));

      mule4QueryOperation.addContent(mule4Query);
    }

    Element mule3Headers = mule3QueryOperation.getChild("headers", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE);
    String fetchSize = mule3QueryOperation.getAttributeValue("fetchSize");
    report.report("salesforce.fetchSize", mule3QueryOperation, mule4QueryOperation);
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

        mule4QueryOperation.setAttribute("headers", expression);
      }

      List<Element> children = mule3Headers.getChildren();
      if (children.size() > 0) {
        Element mule4Headers = new Element("headers", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
        children.stream()
            .forEach(header -> {
              mule4Headers.addContent(
                                      new Element("header", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE)
                                          .setAttribute("key", header.getAttributeValue("key"))
                                          .setAttribute("value", header.getText()));
            });
        mule4QueryOperation.addContent(mule4Headers);

        if (fetchSize != null) {
          mule4Headers.addContent(new Element("header", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE)
              .setAttribute("key", "batchSize")
              .setAttribute("value", fetchSize));
        } else {
          mule4Headers.addContent(new Element("header", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE)
              .setAttribute("key", "batchSize")
              .setAttribute("value", "2000"));
        }
      }
    } else {
      if (fetchSize != null) {
        String expression = expressionMigrator.wrap("{'batchSize':\"" + fetchSize + "\"}");
        mule4QueryOperation.setAttribute("headers", expression);
      } else {
        String expression = expressionMigrator.wrap("{'batchSize':\"2000\"}");
        mule4QueryOperation.setAttribute("headers", expression);
      }
    }

    XmlDslUtils.addElementAfter(mule4QueryOperation, mule3QueryOperation);
    mule3QueryOperation.getParentElement().removeContent(mule3QueryOperation);
  }

  private void setAttributes(Element mule3QueryOperation, Element mule4QueryOperation) {
    String docName = mule3QueryOperation.getAttributeValue("name", SalesforceConstants.DOC_NAMESPACE);
    if (docName != null) {
      mule4QueryOperation.setAttribute("name", docName, SalesforceConstants.DOC_NAMESPACE);
    }

    String notes = mule3QueryOperation.getAttributeValue("description", SalesforceConstants.DOC_NAMESPACE);
    if (notes != null) {
      mule4QueryOperation.setAttribute("description", notes, SalesforceConstants.DOC_NAMESPACE);
    }

    String configRef = mule3QueryOperation.getAttributeValue("config-ref");
    if (configRef != null && !configRef.isEmpty()) {
      mule4QueryOperation.setAttribute("config-ref", configRef);
    }
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return this.expressionMigrator;
  }
}
