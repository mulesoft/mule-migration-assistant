/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;

/**
 * Moves an attribute to a child node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO can we make this a particular case fo MoveAttributeSToChildNode
public class MoveAttributeToChildNode /*extends AbstractMigrationStep */ {

  private String attribute;
  private String childNode;

  public MoveAttributeToChildNode(String attribute, String childNode) {
    setAttribute(attribute);
    setChildNode(childNode);
  }

  public MoveAttributeToChildNode() {}

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
