/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate the domain top-level element
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleDomain extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/ee/domain' and local-name() = 'mule-domain']";

  @Override
  public String getDescription() {
    return "Migrate domain top-level element";
  }

  public MuleDomain() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.removeAttribute("version");
  }

}
