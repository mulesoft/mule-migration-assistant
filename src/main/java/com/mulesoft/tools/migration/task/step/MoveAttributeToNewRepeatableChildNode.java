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

public class MoveAttributeToNewRepeatableChildNode extends MigrationStep {

    private String attribute;
    private String childNode;
    private String newAttributeMappingName;
    private String newAttributeMappingValue;

    public MoveAttributeToNewRepeatableChildNode(String attribute, String childNode, String newAttributeName, String newAttributeValue) {
        setAttribute(attribute);
        setChildNode(childNode);
        setNewAttributeMappingName(newAttributeName);
        setNewAttributeMappingValue(newAttributeValue);
    }

    public MoveAttributeToNewRepeatableChildNode(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                Attribute att = node.getAttribute(getAttribute());
                if (att != null) {
                    node.removeAttribute(att);
                    Element child = node.getChild(getChildNode(), node.getNamespace());
                    if (child == null) {
                        createNewChildNode(node, att);
                    }
                    // If newAttributeMappingName exists in a child node, will only update the value
                    else if (child.getAttribute(getNewAttributeMappingName()) != null && child.getAttribute(getNewAttributeMappingName()).getValue().equals(att.getName())) {
                        child.getAttribute(getNewAttributeMappingValue()).setValue(att.getValue());
                        getReportingStrategy().log("Moved attribute " + att.getName() + "=\"" + att.getValue() + "\" to an already existing child node <" + child.getQualifiedName() + "> with key " + getAttribute(), RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                    }
                    // Otherwise, just create the new child node
                    else {
                        createNewChildNode(node, att);
                    }
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
        }
    }

    private void createNewChildNode(Element node, Attribute att) {
        Element newTargetElement = new Element(getChildNode(), node.getNamespace());
        newTargetElement.setAttribute(getNewAttributeMappingName(), att.getName());
        newTargetElement.setAttribute(getNewAttributeMappingValue(), att.getValue());
        node.addContent(newTargetElement);
        getReportingStrategy().log("Moved attribute " + att.getName() + "=\"" + att.getValue() + "\" to new child node <" + newTargetElement.getQualifiedName() + ">", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
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

    public String getNewAttributeMappingName() {
        return newAttributeMappingName;
    }

    public String getNewAttributeMappingValue() {
        return newAttributeMappingValue;
    }

    public void setNewAttributeMappingName(String newAttributeMappingName) {
        this.newAttributeMappingName = newAttributeMappingName;
    }

    public void setNewAttributeMappingValue(String newAttributeMappingValue) {
        this.newAttributeMappingValue = newAttributeMappingValue;
    }
}
