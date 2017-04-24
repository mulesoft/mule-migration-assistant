package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

public class DeleteAttribute extends MigrationStep {

    private String attributeName;

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public DeleteAttribute(String attributeName) {
        setAttributeName(attributeName);
    }

    public DeleteAttribute(){}

    public void execute() throws Exception {
        try {
            for (Element node : this.getNodes()) {
                node.removeAttribute(getAttributeName());
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Remove Attribute step exception. " + ex.getMessage());
        }
    }
}
