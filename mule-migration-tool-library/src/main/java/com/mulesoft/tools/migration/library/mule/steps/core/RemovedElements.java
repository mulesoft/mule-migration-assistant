/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
