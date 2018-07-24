/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
    return "Update batch history expiration attributes.";
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
