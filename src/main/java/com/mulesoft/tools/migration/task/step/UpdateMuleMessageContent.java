package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.message.MuleMessageUtils;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class UpdateMuleMessageContent extends MigrationStep {

    public UpdateMuleMessageContent(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {
                if(null != node.getText()) {
                    node.setText(MuleMessageUtils.replaceContent(node.getText()));

                    getReportingStrategy().log("Mule Message content has been updated for node <" + node.getQualifiedName() + "> to " + node.getText(), RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Update Mule Message Content exception. " + ex.getMessage());
        }
    }
}
