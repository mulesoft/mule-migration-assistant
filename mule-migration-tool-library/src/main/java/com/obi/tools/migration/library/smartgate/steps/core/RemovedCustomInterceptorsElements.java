/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import org.jdom2.Element;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Remove elements from 3.x that have no replacement in 4.x.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemovedCustomInterceptorsElements extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("custom-interceptor");

  @Override
  public String getDescription() {
    return "Remove elements from 3.x that have no replacement in 4.x.";
  }

  public RemovedCustomInterceptorsElements() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("components.removed", object, object, object.getName());
    object.getParent().removeContent(object);
  }
}
