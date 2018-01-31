/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
