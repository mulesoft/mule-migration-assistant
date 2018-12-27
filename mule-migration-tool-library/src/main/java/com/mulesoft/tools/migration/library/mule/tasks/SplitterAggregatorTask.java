package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.splitter.AbstractSplitter;
import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorsModulePomContribution;
import com.mulesoft.tools.migration.library.mule.steps.splitter.AggregatorsNamespaceContribution;
import com.mulesoft.tools.migration.library.mule.steps.splitter.VmConfig;
import com.mulesoft.tools.migration.library.mule.steps.splitter.VmInformation;
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
    VmInformation vmInformation = new VmInformation("dummyName");
    return newArrayList(new VmNamespaceContribution(),new AggregatorsNamespaceContribution(), new AggregatorsModulePomContribution(), new AbstractSplitter(vmInformation), new VmConfig(vmInformation));
  }
}
