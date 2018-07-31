/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsOutboundEndpoint;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for JMS Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate JMS Transport";
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
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new JmsConnectorPomContribution(),
                        new JmsInboundEndpoint(), new JmsOutboundEndpoint(),
                        new JmsConnector());
  }
}
