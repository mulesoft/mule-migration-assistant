/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.vm.VmConnector;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmOutboundEndpoint;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VMMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate VM Transport";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new VmConnectorPomContribution(),
                        new VmGlobalEndpoint(), new VmInboundEndpoint(), new VmOutboundEndpoint(),
                        new VmConnector());
  }
}
