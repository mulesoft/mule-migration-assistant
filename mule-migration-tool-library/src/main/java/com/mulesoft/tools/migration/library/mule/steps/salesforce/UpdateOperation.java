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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;

/**
 * Migrate BatchJob component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateOperation extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String name = "update";

  private ExpressionMigrator expressionMigrator;

  public UpdateOperation() {
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3UpdateOperation, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(SalesforceConstants.MULE4_SALESFORCE_NAMESPACE,
                                       SalesforceConstants.MULE4_SALESFORCE_NAMESPACE_URI, mule3UpdateOperation.getDocument());

    Element mule4UpdateOperation = new Element(name, SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
    setAttributes(mule3UpdateOperation, mule4UpdateOperation, report);

    if (mule3UpdateOperation.getAttribute("accessTokenId") != null) {
      report.report("salesforce.accessTokenId", mule4UpdateOperation, mule4UpdateOperation);
    }

    Optional<Element> mule3Headers =
        Optional.ofNullable(mule3UpdateOperation.getChild("headers", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    mule3Headers.ifPresent(headers -> {
      String refHeaders = headers.getAttributeValue("ref");
      if (refHeaders != null) {
        String expression = expressionMigrator.migrateExpression(refHeaders, true, headers);
        mule4UpdateOperation.setAttribute("headers", expression);
      }

      List<Element> children = headers.getChildren();
      if (children.size() > 0) {
        Element mule4Headers = new Element("headers", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);
        children.stream()
            .forEach(header -> {
              mule4Headers.addContent(
                                      new Element("header", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE)
                                          .setAttribute("key", header.getText())
                                          .setAttribute("value", header.getText()));
            });
        mule4UpdateOperation.addContent(mule4Headers);
      }
    });

    Optional<Element> objects =
        Optional.ofNullable(mule3UpdateOperation.getChild("objects", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    objects.ifPresent(records -> {
      Optional.ofNullable(records.getAttributeValue("ref")).ifPresent(value -> {
        Element recordsChild = new Element("records", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);

        String expression = expressionMigrator.migrateExpression(value, true, records);
        recordsChild.setContent(new CDATA(expression));

        mule4UpdateOperation.addContent(recordsChild);
      });

      List<Element> children = records.getChildren();
      if (children.size() > 0) {
        String transformBody = children.stream()
            .map(object -> object.getChildren().stream()
                .map(innerObject -> innerObject.getAttributeValue("key") + " : \"" + innerObject.getText() + "\"")
                .collect(Collectors.joining(",\n")))
            .collect(Collectors.joining("\n},\n{"));

        Element element = new Element("transform");
        element.setName("transform");
        element.setNamespace(CORE_EE_NAMESPACE);
        element.removeContent();
        element.addContent(new Element("message", CORE_EE_NAMESPACE)
            .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
                .setText("%dw 2.0 output application/json\n---\n[{\n" + transformBody + "\n}]")));

        XmlDslUtils.addElementBefore(element, mule3UpdateOperation);

      }
    });

    XmlDslUtils.addElementAfter(mule4UpdateOperation, mule3UpdateOperation);
    mule3UpdateOperation.getParentElement().removeContent(mule3UpdateOperation);
  }

  private void setAttributes(Element mule3UpdateOperation, Element mule4UpdateOperation, MigrationReport report) {
    String docName = mule3UpdateOperation.getAttributeValue("name", SalesforceConstants.DOC_NAMESPACE);
    if (docName != null) {
      mule4UpdateOperation.setAttribute("name", docName, SalesforceConstants.DOC_NAMESPACE);
    }

    String configRef = mule3UpdateOperation.getAttributeValue("config-ref");
    if (configRef != null && !configRef.isEmpty()) {
      mule4UpdateOperation.setAttribute("config-ref", configRef);
    } else {
      report.report("salesforce.update", mule4UpdateOperation, mule4UpdateOperation);
    }

    String type = mule3UpdateOperation.getAttributeValue("type");
    if (type != null && !type.isEmpty()) {
      mule4UpdateOperation.setAttribute("type", type);
    } else {
      report.report("salesforce.update", mule4UpdateOperation, mule4UpdateOperation);
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
