/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Update Attribute value
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateAttribute extends MigrationStep {

  private String attributeName;
  private String newValue;

  public UpdateAttribute(String attributeName, String newValue) {
    setAttributeName(attributeName);
    setNewValue(newValue);
  }

  public UpdateAttribute() {}

  public void execute() throws Exception {
    try {
      for (Element node : getNodes()) {
        Attribute att = node.getAttribute(getAttributeName());
        if (att != null) {
          att.setValue(getNewValue());

          getReportingStrategy().log(
                                     "Attribute " + att.getName() + "=\"" + att.getValue() + "\" updated it's value to"
                                         + att.getName() + "=\"" + newValue + "\"",
                                     RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Update attribute exception. " + ex.getMessage());
    }
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }
}
