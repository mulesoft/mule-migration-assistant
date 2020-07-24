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
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;

/**
 * Migrate Upsert operation
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RetrieveOperation extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private static final String name = "retrieve";

  private ExpressionMigrator expressionMigrator;

  public RetrieveOperation() {
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE_URI, name, false));
    this.setNamespacesContributions(newArrayList(SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  @Override
  public void execute(Element mule3RetrieveOperation, MigrationReport report) throws RuntimeException {

    getApplicationModel().addNameSpace(SalesforceConstants.MULE4_SALESFORCE_NAMESPACE,
                                       SalesforceConstants.MULE4_SALESFORCE_NAMESPACE_URI, mule3RetrieveOperation.getDocument());

    Element mule4RetrieveOperation = new Element(name, SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);

    setAttributes(mule3RetrieveOperation, mule4RetrieveOperation);

    if (mule3RetrieveOperation.getAttribute("accessTokenId") != null) {
      report.report("salesforce.accessTokenId", mule3RetrieveOperation, mule4RetrieveOperation);
    }

    Optional<Element> mule3Headers =
        Optional.ofNullable(mule3RetrieveOperation.getChild("headers", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    mule3Headers.ifPresent(headers -> {
      String refHeaders = headers.getAttributeValue("ref");
      if (refHeaders != null) {
        String expression = expressionMigrator.migrateExpression(refHeaders, true, headers);
        mule4RetrieveOperation.setAttribute("headers", expression);
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
        mule4RetrieveOperation.addContent(mule4Headers);
      }
    });

    Optional<Element> ids =
        Optional.ofNullable(mule3RetrieveOperation.getChild("ids", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    Optional<Element> fields =
        Optional.ofNullable(mule3RetrieveOperation.getChild("fields", SalesforceConstants.MULE3_SALESFORCE_NAMESPACE));

    fields.ifPresent(retrieveRequest -> {
      Optional.ofNullable(retrieveRequest.getAttributeValue("ref")).ifPresent(value -> {
        Element recordsChild = new Element("retrieve-request", SalesforceConstants.MULE4_SALESFORCE_NAMESPACE);

        String expression = expressionMigrator.migrateExpression(value, true, retrieveRequest);
        recordsChild.setContent(new CDATA(expression));

        mule4RetrieveOperation.addContent(recordsChild);
      });

      List<Element> children = retrieveRequest.getChildren(); //cand sunt manual facute
      if (children.size() > 0) {
        String filtersTransformBody = "fields : [" + children.stream()
                .map(object -> object.getContent().stream()
                        .map(innerObject -> "\"" + innerObject.getValue() + "\"")
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining(",")) + "]";

        getApplicationModel().addNameSpace(CORE_EE_NAMESPACE, EE_NAMESPACE_SCHEMA, mule3RetrieveOperation.getDocument());
        Element element = new Element("transform");
        element.setName("transform");
        element.setNamespace(CORE_EE_NAMESPACE);
        element.removeContent();
        element.addContent(new Element("message", CORE_EE_NAMESPACE)
            .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
                .setText("%dw 2.0 output application/json\n---\n[{\n" + filtersTransformBody + "\n}]")));

        XmlDslUtils.addElementBefore(element, mule3RetrieveOperation);
      }
    });

    XmlDslUtils.addElementAfter(mule4RetrieveOperation, mule3RetrieveOperation);
    mule3RetrieveOperation.getParentElement().removeContent(mule3RetrieveOperation);
  }

  private void setAttributes(Element mule3RetrieveOperation, Element mule4RetrieveOperation) {

    String docName = mule3RetrieveOperation.getAttributeValue("name", SalesforceConstants.DOC_NAMESPACE);
    if (docName != null) {
      mule4RetrieveOperation.setAttribute("name", docName, SalesforceConstants.DOC_NAMESPACE);
    }

    String configRef = mule3RetrieveOperation.getAttributeValue("config-ref");
    if (configRef != null && !configRef.isEmpty()) {
      mule4RetrieveOperation.setAttribute("config-ref", configRef);
    }

    String type = mule3RetrieveOperation.getAttributeValue("type");
    if (type != null && !type.isEmpty()) {
      mule4RetrieveOperation.setAttribute("objectType", type);
    }

    String notes = mule3RetrieveOperation.getAttributeValue("description", SalesforceConstants.DOC_NAMESPACE);
    if (notes != null) {
      mule4RetrieveOperation.setAttribute("description", notes, SalesforceConstants.DOC_NAMESPACE);
    }
  }
}
