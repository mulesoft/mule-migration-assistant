/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.tools.dw.DataweaveUtils;
import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MigrateDataweaveScript extends MigrationStep {

    public MigrateDataweaveScript(){}

    public void execute() throws Exception {
        try {
            for (Element node : getNodes()) {

                if(!isEmpty(node.getText())) {
                    node.setText(DataweaveUtils.getMigratedScript(node.getText()));
                    getReportingStrategy().log("Dataweave script has been migrated to Dataweave 2", RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Move attribute to MEL content exception. " + ex.getMessage());
        }
    }
}
