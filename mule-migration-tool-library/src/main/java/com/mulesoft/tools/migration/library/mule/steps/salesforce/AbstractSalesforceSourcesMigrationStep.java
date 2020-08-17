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

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;

/**
 * Migrate Abstract Salesforce Application Migration Step
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AbstractSalesforceSourcesMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private final String mule3Name;
  private final String mule4Name;
  protected ExpressionMigrator expressionMigrator;
  protected Element mule4Source;

  public AbstractSalesforceSourcesMigrationStep(String mule3Name, String mule4Name) {
    this.mule3Name = mule3Name;
    this.mule4Name = mule4Name;
  }

  @Override
  public void execute(Element mule3Source, MigrationReport report) throws RuntimeException {
    addNameSpace(SalesforceUtils.MULE4_SALESFORCE_NAMESPACE,
                 SalesforceUtils.MULE4_SALESFORCE_SCHEMA_LOCATION, mule3Source.getDocument());
    mule4Source = new Element(getMule4Name(), SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
    setDefaultAttributes(mule3Source, mule4Source);
  }

  private void setDefaultAttributes(Element mule3Source, Element mule4Source) {
    String docName = mule3Source.getAttributeValue("name", SalesforceUtils.DOC_NAMESPACE);
    if (docName != null) {
      mule4Source.setAttribute("name", docName, SalesforceUtils.DOC_NAMESPACE);
    }

    String notes = mule3Source.getAttributeValue("description", SalesforceUtils.DOC_NAMESPACE);
    if (notes != null) {
      mule4Source.setAttribute("description", notes, SalesforceUtils.DOC_NAMESPACE);
    }

    String configRef = mule3Source.getAttributeValue("config-ref");
    if (configRef != null && !configRef.isEmpty()) {
      mule4Source.setAttribute("config-ref", configRef);
    }
  }

  public String getMule4Name() {
    return mule4Name;
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
