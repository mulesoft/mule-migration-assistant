/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.message.MuleMessageUtils;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Ut know how to update the content of a message
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO change this to update event, also although generic this should almost always be called from a task component
public class UpdateMuleMessageContent extends MigrationStep {

  public UpdateMuleMessageContent() {}

  public void execute() throws Exception {
    try {
      for (Element node : getNodes()) {
        if (null != node.getText()) {
          node.setText(MuleMessageUtils.replaceContent(node.getText()));

          getReportingStrategy()
              .log("Mule Message content has been updated for node <" + node.getQualifiedName() + "> to " + node.getText(),
                   RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
        }
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Update Mule Message Content exception. " + ex.getMessage());
    }
  }
}
