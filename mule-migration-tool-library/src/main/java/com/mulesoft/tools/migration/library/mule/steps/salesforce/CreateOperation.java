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

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Migrate BatchJob component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CreateOperation extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String name = "create";

  private ExpressionMigrator expressionMigrator;

  public CreateOperation() {
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3CreateOperation, MigrationReport report) throws RuntimeException {
    Element mule4CreateOperation = new Element(name, SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
    setAttributes(mule3CreateOperation, mule4CreateOperation, report);

    if (mule3CreateOperation.getAttribute("accessTokenId") != null) {
      report.report("salesforce.accessTokenId", mule4CreateOperation, mule4CreateOperation);
    }

    Optional<Element> mule3Headers =
        Optional.ofNullable(mule3CreateOperation.getChild("headers", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    mule3Headers.ifPresent(headers -> {
      String refHeaders = headers.getAttributeValue("ref");
      if (refHeaders != null) {
        String expression = expressionMigrator.migrateExpression(refHeaders, true, headers);
        mule4CreateOperation.setAttribute("headers", expression);
      }
    });

    Optional<Element> objects =
        Optional.ofNullable(mule3CreateOperation.getChild("objects", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    objects.ifPresent(records -> {
      Element recordsChild = new Element("records", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
      Optional.ofNullable(records.getAttributeValue("ref")).ifPresent(value -> {
        String expression = expressionMigrator.migrateExpression(value, true, records);
        recordsChild.setContent(new CDATA(expression));
      });
      mule4CreateOperation.addContent(recordsChild);
    });

    XmlDslUtils.addElementAfter(mule4CreateOperation, mule3CreateOperation);
    mule3CreateOperation.getParentElement().removeContent(mule3CreateOperation);
  }

  private void setAttributes(Element mule3CreateOperation, Element mule4CreateOperation, MigrationReport report) {
    String docName = mule3CreateOperation.getAttributeValue("name", SalesforceConstants.DOC_NAMESPACE);
    if (docName != null) {
      mule4CreateOperation.setAttribute("name", docName, SalesforceConstants.DOC_NAMESPACE);
    }

    String configRef = mule3CreateOperation.getAttributeValue("config-ref");
    if (configRef != null && !configRef.isEmpty()) {
      mule4CreateOperation.setAttribute("config-ref", configRef);
    } else {
      report.report("salesforce.create", mule4CreateOperation, mule4CreateOperation);
    }

    String type = mule3CreateOperation.getAttributeValue("type");
    if (type != null && !type.isEmpty()) {
      mule4CreateOperation.setAttribute("type", type);
    } else {
      report.report("salesforce.create", mule4CreateOperation, mule4CreateOperation);
    }
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
