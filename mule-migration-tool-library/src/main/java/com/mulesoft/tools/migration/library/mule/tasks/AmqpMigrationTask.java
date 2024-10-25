/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpAck;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpConnector;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpReject;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for AMQP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AmqpMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate AMQP Transport";
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
    return newArrayList(new AmqpConnectorPomContribution(),
                        new AmqpInboundEndpoint(),
                        new AmqpOutboundEndpoint(),
                        new AmqpAck(),
                        new AmqpGlobalEndpoint(),
                        new AmqpReject(),
                        new AmqpConnector());
  }
}
