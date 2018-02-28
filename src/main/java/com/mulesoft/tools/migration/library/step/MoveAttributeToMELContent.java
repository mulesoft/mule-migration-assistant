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

import static com.mulesoft.tools.migration.library.tools.mel.MELUtils.getMELExpressionFromValue;
import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Transform and attribute to MEL
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MoveAttributeToMELContent extends MigrationStep {

  private String attributeName;

  public MoveAttributeToMELContent(String attributeName) {
    setAttributeName(attributeName);
  }

  public MoveAttributeToMELContent() {}

  public void execute() throws Exception {
    try {
      for (Element node : getNodes()) {
        Attribute att = node.getAttribute(getAttributeName());
        if (att != null) {
          node.removeAttribute(att);
          node.setText(getMELExpressionFromValue(att.getValue()));

          getReportingStrategy().log("Attribute" + att + "was replaced by the following MEL: " + node.getText(), RULE_APPLIED,
                                     this.getDocument().getBaseURI(), null, this);
        }
      }
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
