/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step.other;

import com.mulesoft.tools.migration.engine.MigrationStep;
import com.mulesoft.tools.migration.engine.exception.MigrationStepException;

/**
 * @author Mulesoft Inc.
 */
public class MunitToolsAssertTrue extends MigrationStep {

  private static final String XPATH_SELECTOR = "//munit:test/*[contains(local-name(),'true')]";

  public void execute() throws Exception {
    try {

      getApplicationModel().replaceNodeName("munit-tools", "assert-that", XPATH_SELECTOR);
      getApplicationModel().updateAttributeName("condition", "expression", XPATH_SELECTOR);
      getApplicationModel().addAttribute("is", "#[equalTo(true)]", XPATH_SELECTOR);

    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }

}
