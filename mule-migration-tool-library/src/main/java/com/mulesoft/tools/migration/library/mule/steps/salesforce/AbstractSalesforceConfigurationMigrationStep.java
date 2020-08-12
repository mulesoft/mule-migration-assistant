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
 * Migrate Abstract Salesforce Application  Configuration Migration Step
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AbstractSalesforceConfigurationMigrationStep extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private final String name;
  protected ExpressionMigrator expressionMigrator;
  protected Element mule4Config;
  protected Element mule4Connection;
  private final String mule4Name;

  public AbstractSalesforceConfigurationMigrationStep(String name, String mule4Name) {
    this.name = name;
    this.mule4Name = mule4Name;
  }

  @Override
  public void execute(Element mule3Config, MigrationReport report) throws RuntimeException {
    addNameSpace(SalesforceUtils.MULE4_SALESFORCE_NAMESPACE,
                 SalesforceUtils.MULE4_SALESFORCE_SCHEMA_LOCATION, mule3Config.getDocument());

    mule4Config = new Element(getName(), SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
    setDefaultAttributes(mule3Config, mule4Config, report);
    setDefaultConnectionAttributes(mule3Config, mule4Config, mule4Name);
  }

  private void setDefaultAttributes(Element mule3Config, Element mule4Config, MigrationReport report) {
    String nameValue = mule3Config.getAttributeValue("name");
    if (nameValue != null) {
      mule4Config.setAttribute("name", nameValue);
    }

    String docName = mule3Config.getAttributeValue("name", SalesforceUtils.DOC_NAMESPACE);
    if (docName != null) {
      mule4Config.setAttribute("name", docName, SalesforceUtils.DOC_NAMESPACE);
    }

    String fetchAllApexSoapMetadataValue = mule3Config.getAttributeValue("fetchAllApexSoapMetadata");
    if (fetchAllApexSoapMetadataValue != null) {
      mule4Config.setAttribute("fetchAllApexSoapMetadata", fetchAllApexSoapMetadataValue);
    }

    String fetchAllApexRestMetadataValue = mule3Config.getAttributeValue("fetchAllApexRestMetadata");
    if (fetchAllApexRestMetadataValue != null) {
      mule4Config.setAttribute("fetchAllApexRestMetadata", fetchAllApexRestMetadataValue);
    }
  }

  private void setDefaultConnectionAttributes(Element mule3Config, Element mule4Config, String mule4Name) {
    mule4Connection = new Element(mule4Name, SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);

    String usernameValue = mule3Config.getAttributeValue("username");
    if (usernameValue != null) {
      mule4Connection.setAttribute("username", usernameValue);
    }

    String passwordValue = mule3Config.getAttributeValue("password");
    if (passwordValue != null) {
      mule4Connection.setAttribute("password", passwordValue);
    }

    String securityTokenValue = mule3Config.getAttributeValue("securityToken");
    if (securityTokenValue != null) {
      mule4Connection.setAttribute("securityToken", securityTokenValue);
    }

    String readTimeoutValue = mule3Config.getAttributeValue("readTimeout");
    if (readTimeoutValue != null) {
      mule4Connection.setAttribute("readTimeout", readTimeoutValue);
    }

    String connectionTimeoutValue = mule3Config.getAttributeValue("connectionTimeout");
    if (connectionTimeoutValue != null) {
      mule4Connection.setAttribute("connectionTimeout", connectionTimeoutValue);
    }



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
