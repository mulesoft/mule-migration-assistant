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

import java.util.List;

import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpAck;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpConnector;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpReject;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

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
    return newArrayList(new AmqpConnectorPomContribution(), new AmqpInboundEndpoint(), new AmqpOutboundEndpoint(), new AmqpAck(),
                        new AmqpGlobalEndpoint(), new AmqpReject(), new AmqpConnector());
  }
}
