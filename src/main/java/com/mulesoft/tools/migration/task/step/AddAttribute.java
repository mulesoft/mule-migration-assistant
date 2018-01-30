package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class AddAttribute extends MigrationStep {

    private String attributeName;
    private String attributeValue;

    public AddAttribute(String attributeName, String attributeValue) {
        setAttributeValue(attributeValue);
        setAttributeName(attributeName);
    }

    public AddAttribute(){}

    public void execute() throws Exception {
        try {
            for (Element node : this.getNodes()) {
                Attribute att = new Attribute(getAttributeName(), getAttributeValue());
                node.setAttribute(att);

                getReportingStrategy().log("Added attribute: " + attributeName + "=\""+ attributeValue + "\" into <" + node.getQualifiedName() + ">", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
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
