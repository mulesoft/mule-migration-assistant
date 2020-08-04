/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.List;
import java.util.Optional;

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;

/**
 * Migrate Abstract Salesforce Application Migration Step
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AbstractSalesforceOperationMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private final String name;
  protected ExpressionMigrator expressionMigrator;
  protected Element mule4Operation;

  public AbstractSalesforceOperationMigrationStep(String name) {
    this.name = name;
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    addNameSpace(SalesforceUtils.MULE4_SALESFORCE_NAMESPACE,
            SalesforceUtils.MULE4_SALESFORCE_NAMESPACE_URI, mule3Operation.getDocument());
    mule4Operation = new Element(getName(), SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
    setDefaultAttributes(mule3Operation, mule4Operation);
    migrateHeaders(mule3Operation, mule4Operation, report);
  }

  private void setDefaultAttributes(Element mule3Operation, Element mule4Operation) {
    String docName = mule3Operation.getAttributeValue("name", SalesforceUtils.DOC_NAMESPACE);
    if (docName != null) {
      mule4Operation.setAttribute("name", docName, SalesforceUtils.DOC_NAMESPACE);
    }

    String notes = mule3Operation.getAttributeValue("description", SalesforceUtils.DOC_NAMESPACE);
    if (notes != null) {
      mule4Operation.setAttribute("description", notes, SalesforceUtils.DOC_NAMESPACE);
    }

    String configRef = mule3Operation.getAttributeValue("config-ref");
    if (configRef != null && !configRef.isEmpty()) {
      mule4Operation.setAttribute("config-ref", configRef);
    }
  }

  protected void migrateHeaders(Element mule3Operation, Element mule4Operation, MigrationReport report) {
    Optional<Element> mule3Headers =
        Optional.ofNullable(mule3Operation.getChild("headers", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));

    mule3Headers.ifPresent(headers -> {
      String refHeaders = headers.getAttributeValue("ref");
      if (refHeaders != null) {
        String expression = expressionMigrator.migrateExpression(refHeaders, true, headers);
        mule4Operation.setAttribute("headers", expression);
      }

      List<Element> children = headers.getChildren();
      if (children.size() > 0) {
        Element mule4Headers = new Element("headers", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        children.forEach(header -> {
              mule4Headers.addContent(
                                      new Element("header", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE)
                                          .setAttribute("key", header.getAttributeValue("key"))
                                          .setAttribute("value", header.getText()));
            });
        mule4Operation.addContent(mule4Headers);
      }
    });
  }

  public String getName() {
    return name;
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
