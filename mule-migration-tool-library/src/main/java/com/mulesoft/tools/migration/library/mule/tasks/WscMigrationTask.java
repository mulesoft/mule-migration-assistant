/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
