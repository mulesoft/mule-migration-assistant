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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;

/**
 * Migrate Retrieve operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RetrieveOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String name = "retrieve";

  public RetrieveOperation() {
    super(name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    super.execute(mule3Operation, report);
    resolveAttributes(mule3Operation, mule4Operation, report);
    setIdsAndFieldsElement(mule3Operation, mule4Operation);

    XmlDslUtils.addElementAfter(mule4Operation, mule3Operation);
    mule3Operation.getParentElement().removeContent(mule3Operation);
  }

  private void resolveAttributes(Element mule3Operation, Element mule4Operation, MigrationReport report) {
    String type = mule3Operation.getAttributeValue("type");
    if (type != null && !type.isEmpty()) {
      mule4Operation.setAttribute("type", type);
    }

    if (mule3Operation.getAttribute("accessTokenId") != null) {
      report.report("salesforce.accessTokenId", mule3Operation, this.mule4Operation);
    }
  }

  private void setIdsAndFieldsElement(Element mule3RetrieveOperation, Element mule4RetrieveOperation) {
    Optional<Element> ids =
        Optional.ofNullable(mule3RetrieveOperation.getChild("ids", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));

    Optional<Element> fields =
        Optional.ofNullable(mule3RetrieveOperation.getChild("fields", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));

    StringBuilder transformBody = new StringBuilder();

    fields.ifPresent(retrieveFields -> {

      Optional.ofNullable(retrieveFields.getAttributeValue("ref")).ifPresent(value -> {
        String expression = expressionMigrator.migrateExpression(value, true, retrieveFields);
        transformBody.append("fields: [" + expressionMigrator.unwrap(expression) + "]");
      });

      List<Element> fieldsChildren = retrieveFields.getChildren();
      if (fieldsChildren.size() > 0) {
        String filtersTransformBody = "fields: [" + fieldsChildren.stream()
            .map(object -> object.getContent().stream()
                .map(innerObject -> "\"" + innerObject.getValue() + "\"")
                .collect(Collectors.joining("")))
            .collect(Collectors.joining(",")) + "]";
        transformBody.append(filtersTransformBody);
      }

      ids.ifPresent(retrieveIds -> {
        Optional.ofNullable(retrieveIds.getAttributeValue("ref")).ifPresent(value -> {
          String expression = expressionMigrator.migrateExpression(value, true, retrieveIds);
          if (transformBody != null && !transformBody.equals("")) {
            transformBody.append(", \n");
          }
          transformBody.append("ids: [" + expressionMigrator.unwrap(expression) + "]");
        });

        List<Element> idsChildren = retrieveIds.getChildren();
        if (idsChildren.size() > 0) {
          String idsTransformBody = "ids: [" + idsChildren.stream()
              .map(object -> object.getContent().stream()
                  .map(innerObject -> "\"" + innerObject.getValue() + "\"")
                  .collect(Collectors.joining("")))
              .collect(Collectors.joining(",")) + "]";


          if (transformBody != null && !transformBody.equals("")) {
            transformBody.append(", \n");
          }
          transformBody.append(idsTransformBody);

        }
      });

      addNameSpace(CORE_EE_NAMESPACE, EE_NAMESPACE_SCHEMA, mule3RetrieveOperation.getDocument());
      Element element = new Element("transform");
      element.setName("transform");
      element.setNamespace(CORE_EE_NAMESPACE);
      element.removeContent();
      element.addContent(new Element("message", CORE_EE_NAMESPACE)
          .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
              .setContent(new CDATA("%dw 2.0 output application/java\n---\n{\n" + transformBody
                  + "\n} as Object { class : \"org.mule.extension.salesforce.api.core.RetrieveRequest\" }"))));

      XmlDslUtils.addElementBefore(element, mule3RetrieveOperation);
    });
  }
}
