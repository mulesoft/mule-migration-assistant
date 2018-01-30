package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class DeleteNode extends MigrationStep {

    public DeleteNode(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                node.detach();

                getReportingStrategy().log("Node <" + node.getQualifiedName() + "> was deleted", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Remove node exception. " + ex.getMessage());
        }
    }
}
