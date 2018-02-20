/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

import org.jdom2.Element;

import com.mulesoft.tools.migration.exception.MigrationStepException;

/**
 * Removes an attribute from a node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DeleteAttribute extends MigrationStep {

  private String attributeName;

  public DeleteAttribute(String attributeName) {
    setAttributeName(attributeName);
  }

  public DeleteAttribute() {}

  public void execute() throws Exception {
    try {
      for (Element node : this.getNodes()) {
        node.removeAttribute(getAttributeName());

        getReportingStrategy().log("Attribute " + attributeName + " was deleted", RULE_APPLIED, this.getDocument().getBaseURI(),
                                   null, this);
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Remove Attribute step exception. " + ex.getMessage());
    }
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeName() {
    return attributeName;
  }
}
