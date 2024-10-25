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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.tools.SalesforceUtils.MULE4_SALESFORCE_NAMESPACE;
import static com.mulesoft.tools.migration.library.tools.SalesforceUtils.START_TRANSFORM_BODY_TYPE_JAVA;

/**
 * Migrate Upsert Bulk operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpsertBulkOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String m3Name = "upsert-bulk";
  private static final String m4Name = "create-job";

  public UpsertBulkOperation() {
    super(m4Name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, m3Name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    super.execute(mule3Operation, report);
    resolveAttributes(mule3Operation, mule4Operation);

    XmlDslUtils.addElementAfter(mule4Operation, mule3Operation);
    mule3Operation.getParentElement().removeContent(mule3Operation);

    addCreateBatchElement(mule3Operation, mule4Operation);
  }

  private void resolveAttributes(Element mule3Operation, Element mule4Operation) {
    SalesforceUtils.resolveTypeAttribute(mule3Operation, mule4Operation);

    mule4Operation.setAttribute("operation", "upsert");

    String externalIdFieldName = mule3Operation.getAttributeValue("externalIdFieldName");
    StringBuilder requestContents = new StringBuilder();

    if (externalIdFieldName != null && !externalIdFieldName.isEmpty()) {
      requestContents.append("externalIdFieldName: \"" + externalIdFieldName + "\"");
    }

    if (requestContents != null && requestContents.length() != 0) {
      Element createJobRequest = new Element("create-job-request", MULE4_SALESFORCE_NAMESPACE);
      createJobRequest.setContent(new CDATA(START_TRANSFORM_BODY_TYPE_JAVA + requestContents.toString()
          + "\n} as Object { class : \"org.mule.extension.salesforce.api.bulk.CreateJobRequest\" }"));
      mule4Operation.addContent(createJobRequest);
    }
  }

  private void addCreateBatchElement(Element mule3Operation, Element mule4Operation) {

    Element createBatch = new Element("create-batch");
    createBatch.setName("create-batch");
    createBatch.setNamespace(MULE4_SALESFORCE_NAMESPACE);
    createBatch.removeContent();

    createBatch.setAttribute("config-ref", mule4Operation.getAttributeValue("config-ref"));

    createBatch.setAttribute("jobInfoBatch", "#[payload]");

    Optional<Element> objects =
        Optional.ofNullable(mule3Operation.getChild("objects", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));

    objects.ifPresent(records -> {
      SalesforceUtils.migrateRecordsFromExpression(records, createBatch, expressionMigrator, "objects");

      List<Element> children = records.getChildren();
      if (children.size() > 0) {
        String objectsJSON = children.stream()
            .map(object -> object.getChildren().stream()
                .map(innerObject -> innerObject.getAttributeValue("key") + " : \"" + innerObject.getText() + "\"")
                .collect(Collectors.joining(",\n")))
            .collect(Collectors.joining("\n},\n{"));

        Element m4Objects = new Element("objects", SalesforceUtils.MULE4_SALESFORCE_NAMESPACE);
        m4Objects.setContent(new CDATA(SalesforceUtils.START_TRANSFORM_BODY_TYPE_JSON
            + objectsJSON + SalesforceUtils.CLOSE_TRANSFORM_BODY_TYPE_JSON));
        createBatch.addContent(m4Objects);
      }
    });

    XmlDslUtils.addElementAfter(createBatch, mule4Operation);
  }
}
