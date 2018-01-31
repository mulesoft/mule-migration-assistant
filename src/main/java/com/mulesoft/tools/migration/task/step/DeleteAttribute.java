/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class DeleteAttribute extends MigrationStep {

    private String attributeName;

    public DeleteAttribute(String attributeName) {
        setAttributeName(attributeName);
    }

    public DeleteAttribute(){}

    public void execute() throws Exception {
        try {
            for (Element node : this.getNodes()) {
                node.removeAttribute(getAttributeName());

                getReportingStrategy().log("Attribute " + attributeName + " was deleted", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
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
