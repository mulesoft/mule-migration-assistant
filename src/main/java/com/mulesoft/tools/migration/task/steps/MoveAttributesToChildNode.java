package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

public class MoveAttributesToChildNode extends MigrationStep {

    private String attributes;
    private String childNode;

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getChildNode() {
        return childNode;
    }

    public void setChildNode(String childNode) {
        this.childNode = childNode;
    }

    public MoveAttributesToChildNode(String attributes, String childNode) {
        setAttributes(attributes);
        setChildNode(childNode);
    }

    public MoveAttributesToChildNode(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                String [] attributesArray = getAttributes().split(";");
                for (String attributeString : attributesArray){
                    Attribute att = node.getAttribute(attributeString);
                    if (att != null) {
                        Element child = node.getChild(getChildNode(), node.getNamespace());
                        if (child != null) {
                            node.removeAttribute(att);
                            child.setAttribute(att);
                        }
                    }
                }
            }
        }catch (Exception ex) {
            throw new MigrationStepException("Move attribute exception. " + ex.getMessage());
        }
    }
}
