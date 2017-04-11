package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

public class UpdateAttributeName extends MigrationStep {

    private String attributeName;
    private String newName;

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
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Update attribute name exception. " + ex.getMessage());
        }
    }

}
