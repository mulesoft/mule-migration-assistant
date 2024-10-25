/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import static com.mulesoft.tools.migration.library.mule.steps.batch.BatchJob.BATCH_NAMESPACE_URI;

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

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + BATCH_NAMESPACE_URI + "' and local-name() = 'commit']";

  @Override
  public String getDescription() {
    return "Update batch commit to batch aggregator.";
  }

  public BatchCommit() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("aggregator");
  }
}
