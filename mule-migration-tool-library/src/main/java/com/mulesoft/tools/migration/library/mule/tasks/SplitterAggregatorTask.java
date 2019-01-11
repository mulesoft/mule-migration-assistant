/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements;
import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorsModulePomContribution;
import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorsNamespaceContribution;
import com.mulesoft.tools.migration.library.mule.steps.splitter.CollectionSplitter;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmNamespaceContribution;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

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
    return null;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new VmNamespaceContribution(), new AggregatorsNamespaceContribution(),
                        new AggregatorsModulePomContribution(), new CollectionSplitter(),
                        new RemoveSyntheticMigrationGlobalElements());
  }
}
