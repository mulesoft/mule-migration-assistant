/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import org.jdom2.Element;

/**
 * It migrates a DW script from it's original version to DataWeave 2
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrateDataweaveScript extends AbstractApplicationModelMigrationStep {

  public MigrateDataweaveScript() {}

  @Override
  public void execute(Element element) throws RuntimeException {
    try {
      // for (Element node : getNodes()) {
      //
      // if (!isEmpty(node.getText())) {
      // node.setText(DataweaveUtils.getMigratedScript(node.getText()));
      // // getReportingStrategy().log("Dataweave script has been migrated to Dataweave 2", RULE_APPLIED,
      // // this.getDocument().getBaseURI(), null, this);
      // }
      // }
    } catch (Exception ex) {
      throw new MigrationStepException("Move attribute to MEL content exception. " + ex.getMessage());
    }
  }

  @Override
  public String getDescription() {
    return null;
  }
}
