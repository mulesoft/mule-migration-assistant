/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

/**
 * Migrate Batch Commit component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class BatchCommit extends AbstractApplicationModelMigrationStep {

  public static final String BATCH_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/batch";
  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + BATCH_NAMESPACE_URI + "' and local-name() = 'commit']";

  @Override
  public String getDescription() {
    return "Update batch execute to a flow-ref with equal reference name.";
  }

  public BatchCommit() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("aggregator");
  }
}
