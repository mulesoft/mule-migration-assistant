/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.MigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Removes a node
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
// TODO can we make this the same as {@link DeleteChildNode}
public class DeleteNode extends MigrationStep {

  public DeleteNode() {}

  public void execute() throws Exception {
    try {
      for (Element node : getNodes()) {
        node.detach();

        //        getReportingStrategy().log("Node <" + node.getQualifiedName() + "> was deleted", RULE_APPLIED,
        //                                   this.getDocument().getBaseURI(), null, this);
      }
    } catch (Exception ex) {
      throw new MigrationStepException("Remove node exception. " + ex.getMessage());
    }
  }
}
