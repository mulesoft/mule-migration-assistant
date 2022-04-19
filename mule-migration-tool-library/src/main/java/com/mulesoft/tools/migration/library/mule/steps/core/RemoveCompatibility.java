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
 * Compatibility elements removal step.
 *
 * @author Mulesoft Inc.
 */
public class RemoveCompatibility extends AbstractApplicationModelMigrationStep {

  private static final String XPATH_SELECTOR = "//*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/compatibility' or "
      + "local-name()='set-property' or "
      + "local-name()='set-session-variable' or "
      + "local-name()='multipart-to-vars' or "
      + "local-name()='copy-properties' or "
      + "local-name()='remove-property' or "
      + "local-name()='remove-session-variable'"
      + "]";

  public RemoveCompatibility() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }

  @Override
  public boolean shouldReportMetrics() {
    return false;
  }
}
