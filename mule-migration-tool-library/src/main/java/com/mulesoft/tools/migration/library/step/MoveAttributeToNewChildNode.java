/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;

/**
 * Transform an attribute in a child node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MoveAttributeToNewChildNode /*extends AbstractMigrationStep */ {

  private String attribute;
  private String childNode;

  public MoveAttributeToNewChildNode(String attribute, String childNode) {
    setAttribute(attribute);
    setChildNode(childNode);
  }

  public MoveAttributeToNewChildNode() {}

  public void execute() throws Exception {
    try {
      //      for (Element node : getNodes()) {
      //        Attribute att = node.getAttribute(getAttribute());
      //        if (att != null) {
      //          Element child = node.getChild(getChildNode(), node.getNamespace());
      //          if (child != null) {
      //            node.removeAttribute(att);
      //            child.setAttribute(att);
      //
      //            //            getReportingStrategy().log(
      //            //                                       "Moved attribute " + att.getName() + "=\"" + att.getValue() + "\" to child node <"
      //            //                                           + child.getQualifiedName() + ">",
      //            //                                       RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
      //          } else {
      //            Element newChild = new Element(getChildNode(), node.getNamespace());
      //            node.removeAttribute(att);
      //            newChild.setAttribute(att);
      //            node.addContent(newChild);
      //
      //            //            getReportingStrategy().log(
      //            //                                       "Moved attribute " + att.getName() + "=\"" + att.getValue() + "\" to new child node <"
      //            //                                           + newChild.getQualifiedName() + ">",
      //            //                                       RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
      //          }
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
    }
  }

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  public String getChildNode() {
    return childNode;
  }

  public void setChildNode(String childNode) {
    this.childNode = childNode;
  }
}
