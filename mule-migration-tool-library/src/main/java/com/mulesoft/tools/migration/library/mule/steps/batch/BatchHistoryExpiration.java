/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate Batch History Expiration component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class BatchHistoryExpiration extends AbstractApplicationModelMigrationStep {

  public static final String BATCH_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/batch";
  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + BATCH_NAMESPACE_URI + "' and local-name() = 'expiration']";

  @Override
  public String getDescription() {
    return "Update batch history expiration attributes to camel case.";
  }

  public BatchHistoryExpiration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Attribute maxAge = object.getAttribute("max-age");
    if (maxAge != null) {
      maxAge.setName("maxAge");
    }
    Attribute ageUnit = object.getAttribute("age-unit");
    if (ageUnit != null) {
      ageUnit.setName("ageUnit");
    }
  }
}
