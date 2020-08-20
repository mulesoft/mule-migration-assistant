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
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;

/**
 * Migrate Cached Basic configuration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CachedBasicConfiguration extends AbstractSalesforceConfigurationMigrationStep implements ExpressionMigratorAware {

  private static final String MULE3_NAME = "cached-basic-config";
  private static final String MULE4_CONFIG = "sfdc-config";
  private static final String MULE4_NAME = "basic-connection";

  private ExpressionMigrator expressionMigrator;

  public CachedBasicConfiguration() {
    super(MULE4_CONFIG, MULE4_NAME);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, MULE3_NAME, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3CachedBasicConfig, MigrationReport report) throws RuntimeException {
    super.execute(mule3CachedBasicConfig, report);

    Optional<Element> mule3ApexConfiguration =
        Optional.ofNullable(mule3CachedBasicConfig.getChild("apex-class-names", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
    mule3ApexConfiguration.ifPresent(apexClassNames -> {
      Element apexClassNameChild = apexClassNames.getChild("apex-class-name", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE);
      String apexClassNameValue = apexClassNameChild.getText();
      if (apexClassNameValue != null) {
        Element mule4ApexClassNames = new Element("apex-class-names", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        Element mule4ApexClassNameChild = new Element("apex-class-name", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        mule4ApexClassNameChild.setAttribute("value", apexClassNameValue);
        mule4ApexClassNames.addContent(mule4ApexClassNameChild);
        mule4Config.addContent(mule4ApexClassNames);
      }
    });

    XmlDslUtils.addElementAfter(mule4Config, mule3CachedBasicConfig);
    mule3CachedBasicConfig.getParentElement().removeContent(mule3CachedBasicConfig);
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
