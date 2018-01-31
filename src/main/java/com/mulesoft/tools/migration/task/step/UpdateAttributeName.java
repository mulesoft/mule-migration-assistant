/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class UpdateAttributeName extends MigrationStep {

    private String attributeName;
    private String newName;

    public UpdateAttributeName(String attributeName, String newName) {
        setAttributeName(attributeName);
        setNewName(newName);
    }

    public UpdateAttributeName() {}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                Attribute att = node.getAttribute(getAttributeName());
                if (att != null) {
                    att.setName(getNewName());

                    getReportingStrategy().log("Attribute " + attributeName + " updated it's name to " + getNewName(), RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                }
            }
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
