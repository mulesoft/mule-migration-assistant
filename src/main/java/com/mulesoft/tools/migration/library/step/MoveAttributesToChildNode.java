/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.MigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Moves attributes to a child node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MoveAttributesToChildNode extends MigrationStep {

  private String attributes;
  private String childNode;

  public MoveAttributesToChildNode(String attributes, String childNode) {
    setAttributes(attributes);
    setChildNode(childNode);
  }

  public MoveAttributesToChildNode() {}

  public void execute() throws Exception {
    try {
      for (Element node : getNodes()) {
        String[] attributesArray = getAttributes().split(";");
        for (String attributeString : attributesArray) {
          Attribute att = node.getAttribute(attributeString);
          if (att != null) {
            Element child = node.getChild(getChildNode(), node.getNamespace());
            if (child != null) {
              node.removeAttribute(att);
              child.setAttribute(att);

              getReportingStrategy().log(
                                         "Moved attribute " + att.getName() + "=\"" + att.getValue() + "\" to child node <"
                                             + node.getQualifiedName() + ">",
                                         RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
            }
          }
        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
    }
  }

  public String getAttributes() {
    return attributes;
  }

  public void setAttributes(String attributes) {
    this.attributes = attributes;
  }

  public String getChildNode() {
    return childNode;
  }

  public void setChildNode(String childNode) {
    this.childNode = childNode;
  }
}
