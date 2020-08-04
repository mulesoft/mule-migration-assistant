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
import org.jdom2.Element;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;

/**
 * Migrate Create operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CreateOperation extends AbstractSalesforceOperationMigrationStep implements ExpressionMigratorAware {

  private static final String name = "create";

  public CreateOperation() {
    super(name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Operation, MigrationReport report) throws RuntimeException {
    super.execute(mule3Operation, report);
    resolveAttributes(mule3Operation, mule4Operation);

    Optional<Element> objects =
        Optional.ofNullable(mule3Operation.getChild("objects", SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));

    objects.ifPresent(records -> {
      SalesforceUtils.migrateRecordsFromExpression(records, mule4Operation, expressionMigrator);

      List<Element> children = records.getChildren();
      if (children.size() > 0) {
        String transformBody = children.stream()
            .map(object -> object.getChildren().stream()
                .map(innerObject -> innerObject.getAttributeValue("key") + " : \"" + innerObject.getText() + "\"")
                .collect(Collectors.joining(",\n")))
            .collect(Collectors.joining("\n},\n{"));


        addNameSpace(CORE_EE_NAMESPACE, EE_NAMESPACE_SCHEMA, mule3Operation.getDocument());
        SalesforceUtils.createTransformBeforeElement(mule3Operation, SalesforceUtils.START_TRANSFORM_BODY_TYPE_JSON
            + transformBody + SalesforceUtils.CLOSE_TRANSFORM_BODY_TYPE_JSON);
      }
    });

    XmlDslUtils.addElementAfter(mule4Operation, mule3Operation);
    mule3Operation.getParentElement().removeContent(mule3Operation);
  }

  private void resolveAttributes(Element mule3Operation, Element mule4Operation) {
    String type = mule3Operation.getAttributeValue("type");
    if (type != null && !type.isEmpty()) {
      mule4Operation.setAttribute("type", type);
    }
  }
}
