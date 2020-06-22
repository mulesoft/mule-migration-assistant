/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove elements from 3.x that have no replacement in 4.x.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemovedElements extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = ""
      + "//*["
      + "local-name()='static-component' or "
      + "local-name()='dynamic-all' or "
      + "local-name()='interceptor-stack'"
      + "]";

  @Override
  public String getDescription() {
    return "Remove elements from 3.x that have no replacement in 4.x.";
  }

  public RemovedElements() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("components.removed", object, object, object.getName());
  }

}
