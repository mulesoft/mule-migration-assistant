/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import static com.google.common.base.Preconditions.*;
import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;

import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Represent the application to be migrated
 * 
 * @author Mulesoft Inc.
 */
public class ApplicationModel {

  private Map<Path, Document> applicationDocuments;

  protected ApplicationModel(Map<Path, Document> applicationDocuments) {
    this.applicationDocuments = applicationDocuments;
  }

  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  public List<Element> getNodes(String xpathExpression) {
    checkArgument(StringUtils.isNotBlank(xpathExpression), "The Xpath Expression must not be null nor empty");

    List<Element> nodes = new ArrayList<>();

    for (Document doc : applicationDocuments.values()) {
      XPathExpression<Element> xpath = getXPathExpression(xpathExpression, doc);
      nodes.addAll(xpath.evaluate(doc));
    }

    return nodes;
  }

  public void replaceNodeName(String nameSpace, String nodeName, String xpath) {
    for (Document doc : applicationDocuments.values()) {
      Namespace namespace = doc.getRootElement().getNamespace(nameSpace);

      for (Element node : getXPathExpression(xpath, doc).evaluate(doc)) {
        node.setNamespace(namespace);
        node.setName(nodeName);
        // TODO missing reporting com.mulesoft.tools.migration.library.step.ReplaceNodesName.execute()
      }
    }
  }

  public void updateAttributeName(String oldName, String newName, String xpath) {
    for (Document doc : applicationDocuments.values()) {

      for (Element node : getXPathExpression(xpath, doc).evaluate(doc)) {
        Attribute attribute = node.getAttribute(oldName);
        if (attribute != null) {
          attribute.setName(newName);
          // TODO missing reporting com.mulesoft.tools.migration.library.step.UpdateAttributeName.execute
        }
      }
    }
  }

  public void addAttribute(String attributeName, String attributeValue, String xpath){
    for (Document doc : applicationDocuments.values()) {

      for (Element node : getXPathExpression(xpath, doc).evaluate(doc)) {
        Attribute att = new Attribute(attributeName, attributeValue);
        node.setAttribute(att);
        // TODO missing reporting com.mulesoft.tools.migration.library.step.AddAttribute.execute
      }
    }
  }

  private XPathExpression<Element> getXPathExpression(String xpath, Document doc) {
    return XPathFactory.instance().compile(xpath, Filters.element(), null, doc.getRootElement().getAdditionalNamespaces());
  }



}
