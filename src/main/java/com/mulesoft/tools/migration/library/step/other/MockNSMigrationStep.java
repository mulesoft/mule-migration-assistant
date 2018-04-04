/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step.other;

import com.mulesoft.tools.migration.engine.step.AbstractMigrationStep;
import com.mulesoft.tools.migration.engine.step.category.NamespaceContribution;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * This steps migrates the MUnit 1.x assert-true
 *
 * @author Mulesoft Inc.
 */
public class MockNSMigrationStep extends AbstractMigrationStep implements NamespaceContribution {

  @Override
  public String getDescription() {
    return null;
  }

  public void execute() throws Exception {
    try {

      ApplicationModel applicationModel = getApplicationModel();

      applicationModel.removeNameSpace("mock", "http://www.mulesoft.org/schema/mule/mock",
                                       "http://www.mulesoft.org/schema/mule/mock/current/mule-mock.xsd");
      applicationModel.addNameSpace("munit-tools", "http://www.mulesoft.org/schema/mule/munit-tools");

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }

}
