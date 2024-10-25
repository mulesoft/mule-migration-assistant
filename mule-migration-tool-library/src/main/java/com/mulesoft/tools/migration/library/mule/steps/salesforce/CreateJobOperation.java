/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.CDATA;
import org.jdom2.Element;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.tools.SalesforceUtils.START_TRANSFORM_BODY_TYPE_JAVA;

/**
 * Migrate Create Job operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CreateJobOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String name = "create-job";

  public CreateJobOperation() {
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
    SalesforceUtils.resolveTypeAttribute(mule3Operation, mule4Operation);

    String operation = mule3Operation.getAttributeValue("operation");
    if (operation != null && !operation.isEmpty()) {
      mule4Operation.setAttribute("operation", operation);
    }

    String externalIdFieldName = mule3Operation.getAttributeValue("externalIdFieldName");
    String concurrencyMode = mule3Operation.getAttributeValue("concurrencyMode");
    String contentType = mule3Operation.getAttributeValue("contentType");

    StringBuilder requestContents = new StringBuilder();

    if (externalIdFieldName != null && !externalIdFieldName.isEmpty()) {
      requestContents.append("externalIdFieldName: \"" + externalIdFieldName + "\"");
    }

    if (concurrencyMode != null && !concurrencyMode.isEmpty()) {
      if (requestContents != null && requestContents.length() != 0) {
        requestContents.append(", \n");
      }
      requestContents.append("concurrencyMode: \"" + concurrencyMode + "\"");
    }

    if (contentType != null && !contentType.isEmpty()) {
      if (requestContents != null && requestContents.length() != 0) {
        requestContents.append(", \n");
      }
      requestContents.append("contentType: \"" + contentType + "\"");
    }

    if (requestContents != null && requestContents.length() != 0) {
      Element createJobRequest = new Element("create-job-request", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
      createJobRequest.setContent(new CDATA(START_TRANSFORM_BODY_TYPE_JAVA + requestContents.toString()
          + "\n} as Object { class : \"org.mule.extension.salesforce.api.bulk.CreateJobRequest\" }"));
      mule4Operation.addContent(createJobRequest);
    }
  }
}
