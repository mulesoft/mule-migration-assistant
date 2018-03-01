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
 * Adds an attribute to a node.
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AddAttribute extends MigrationStep {

  private String attributeName;
  private String attributeValue;

  public AddAttribute(String attributeName, String attributeValue) {
    setAttributeValue(attributeValue);
    setAttributeName(attributeName);
  }

  public AddAttribute() {}

  public void execute() throws Exception {
    try {
      for (Element node : this.getNodes()) {
        Attribute att = new Attribute(getAttributeName(), getAttributeValue());
        node.setAttribute(att);

        //        getReportingStrategy()
        //            .log("Added attribute: " + attributeName + "=\"" + attributeValue + "\" into <" + node.getQualifiedName() + ">",
        //                 RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Add Attribute step exception. " + ex.getMessage());
    }
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getAttributeValue() {
    return attributeValue;
  }
}
