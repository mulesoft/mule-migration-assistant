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
public class CreateChildNodeFromAttribute /*extends AbstractMigrationStep */ {

  private String attribute;

  public CreateChildNodeFromAttribute(String attribute) {
    setAttribute(attribute);
  }

  public CreateChildNodeFromAttribute() {}

  public void execute() throws Exception {
    try {
      //      for (Element node : getNodes()) {
      //        Attribute att = node.getAttribute(getAttribute());
      //        if (att != null) {
      //          Element child = new Element(getAttribute());
      //          child.setNamespace(node.getNamespace());
      //          Attribute newAtt = new Attribute("value", att.getValue());
      //          child.setAttribute(newAtt);
      //          node.addContent(0, child);
      //          node.removeAttribute(att);
      //
      //          //          getReportingStrategy().log("Child node from attribute created:" + attribute, RULE_APPLIED,
      //          //                                     this.getDocument().getBaseURI(), null, this);
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Create child node exception. " + ex.getMessage());
    }
  }

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }
}
