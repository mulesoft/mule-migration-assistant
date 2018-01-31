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

public class MoveAttributeToChildNode extends MigrationStep {

    private String attribute;
    private String childNode;

    public MoveAttributeToChildNode(String attribute, String childNode) {
        setAttribute(attribute);
        setChildNode(childNode);
    }

    public MoveAttributeToChildNode(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                Attribute att = node.getAttribute(getAttribute());
                if (att != null) {
                    Element child = node.getChild(getChildNode(), node.getNamespace());
                    if (child != null) {
                        node.removeAttribute(att);
                        child.setAttribute(att);

                        getReportingStrategy().log("Moved attribute " + att.getName() + "=\""+ att.getValue() + "\" to child node <" + child.getQualifiedName() + ">", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                    }
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
        }
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getChildNode() {
        return childNode;
    }

    public void setChildNode(String childNode) {
        this.childNode = childNode;
    }
}
