/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.cxf.CxfModuleNamespaceMigrator;
import com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumer;
import com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumerConfig;
import com.mulesoft.tools.migration.library.mule.steps.wsc.WsConsumerPomContribution;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for WSC/CXF components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class WscMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate WSC/CXF components";
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
    return newArrayList(new WsConsumerPomContribution(),
                        new WsConsumerConfig(),
                        new WsConsumer(),

                        new CxfModuleNamespaceMigrator());
  }
}
