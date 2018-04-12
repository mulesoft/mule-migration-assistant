/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;

/**
 * Update the name of an attribute
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UpdateAttributeName /*extends AbstractMigrationStep */ {

  private String attributeName;
  private String newName;

  public UpdateAttributeName(String attributeName, String newName) {
    setAttributeName(attributeName);
    setNewName(newName);
  }

  public UpdateAttributeName() {}

  public void execute() throws Exception {
    try {
      //      for (Element node : getNodes()) {
      //        Attribute att = node.getAttribute(getAttributeName());
      //        if (att != null) {
      //          att.setName(getNewName());
      //
      //          //          getReportingStrategy().log("Attribute " + attributeName + " updated it's name to " + getNewName(), RULE_APPLIED,
      //          //                                     this.getDocument().getBaseURI(), null, this);
      //        }
      //      }
    } catch (Exception ex) {
      throw new MigrationStepException("Update attribute name exception. " + ex.getMessage());
    }
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

}
