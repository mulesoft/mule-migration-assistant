/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorWithNoSplitter;
import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorsModulePomContribution;
import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorsNamespaceContribution;
import com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter;
import com.mulesoft.tools.migration.library.mule.steps.splitter.CustomSplitter;
import com.mulesoft.tools.migration.library.mule.steps.splitter.ExpressionSplitter;
import com.mulesoft.tools.migration.library.mule.steps.splitter.MapSplitter;
import com.mulesoft.tools.migration.library.mule.steps.splitter.MessageChunkSplitter;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for splitter and aggregators
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SplitterAggregatorTask extends AbstractMigrationTask {

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getDescription() {
    return "Migrate splitter and aggregator components";
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new AggregatorsModulePomContribution(),
                        new AggregatorsNamespaceContribution(),
                        new CollectionSplitter(),
                        new ExpressionSplitter(),
                        new CustomSplitter(),
                        new MapSplitter(),
                        new MessageChunkSplitter(),
                        new AggregatorWithNoSplitter());
  }
}
