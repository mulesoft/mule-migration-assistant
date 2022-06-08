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
 * Unsupported interceptor elements.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class InterceptorElements extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = ""
      + "//*["
      + "local-name()='interceptor-stack' or "
      + "local-name()='custom-interceptor' or "
      + "local-name()='logging-interceptor' or "
      + "local-name()='timer-interceptor'"
      + "]";

  @Override
  public String getDescription() {
    return "Interceptor elements are no longer supported";
  }

  public InterceptorElements() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("components.interceptors", object, object, object.getName());
  }

}
