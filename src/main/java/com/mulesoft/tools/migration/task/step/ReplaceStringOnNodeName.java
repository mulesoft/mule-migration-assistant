package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class ReplaceStringOnNodeName extends MigrationStep{

    private String stringToReplace;
    private String newValue;

    public ReplaceStringOnNodeName(String stringToReplace, String newValue) {
        setStringToReplace(stringToReplace);
        setNewValue(newValue);
    }

    public ReplaceStringOnNodeName(){}

    public void execute() throws Exception {
        try {
            for (Element node : this.getNodes()) {
                if (node.getName().contains(getStringToReplace())) {
                    String legacyNode = node.getQualifiedName();
                    node.setName(node.getName().replace(getStringToReplace(), getNewValue()));

                    getReportingStrategy().log("The Node <" + legacyNode + "> that contained " + stringToReplace + " was replaced to <" + node.getQualifiedName() + ">", RULE_APPLIED);
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Replace string on node step exception. " + ex.getMessage());
        }
    }

    public String getStringToReplace() {
        return stringToReplace;
    }

    public void setStringToReplace(String stringToReplace) {
        this.stringToReplace = stringToReplace;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
