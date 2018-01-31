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

public class CreateChildNodeFromAttribute extends MigrationStep {

    private String attribute;

    public CreateChildNodeFromAttribute(String attribute) {
        setAttribute(attribute);
    }

    public CreateChildNodeFromAttribute(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                Attribute att = node.getAttribute(getAttribute());
                if (att != null) {
                    Element child = new Element(getAttribute());
                    child.setNamespace(node.getNamespace());
                    Attribute newAtt = new Attribute("value", att.getValue());
                    child.setAttribute(newAtt);
                    node.addContent(0,child);
                    node.removeAttribute(att);

                    getReportingStrategy().log("Child node from attribute created:" + attribute, RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                }
            }
        }catch (Exception ex) {
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
