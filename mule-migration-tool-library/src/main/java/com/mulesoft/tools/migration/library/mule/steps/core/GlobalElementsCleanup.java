/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove global filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class GlobalElementsCleanup extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "/*/*[@*[local-name() = 'globalProcessed' and namespace-uri() = 'migration']]";

  @Override
  public String getDescription() {
    return "Update global filters.";
  }

  public GlobalElementsCleanup() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.detach();

  }
}
