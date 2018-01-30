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
