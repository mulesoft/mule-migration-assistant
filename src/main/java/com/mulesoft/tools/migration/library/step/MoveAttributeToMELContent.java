/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;

/**
 * Transform and attribute to MEL
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MoveAttributeToMELContent /*extends AbstractMigrationStep */ {

  private String attributeName;

  public MoveAttributeToMELContent(String attributeName) {
    setAttributeName(attributeName);
  }

  public MoveAttributeToMELContent() {}

  public void execute() throws Exception {
    try {
      //      for (Element node : getNodes()) {
      //        Attribute att = node.getAttribute(getAttributeName());
      //        if (att != null) {
      //          node.removeAttribute(att);
      //          node.setText(getMELExpressionFromValue(att.getValue()));
      //
      //          //          getReportingStrategy().log("Attribute" + att + "was replaced by the following MEL: " + node.getText(), RULE_APPLIED,
      //          //                                     this.getDocument().getBaseURI(), null, this);
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Move attribute to MEL content exception. " + ex.getMessage());
    }
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }
}
