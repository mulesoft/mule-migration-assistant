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

import com.mulesoft.tools.migration.library.mule.steps.email.EmailConnectorConfig;
import com.mulesoft.tools.migration.library.mule.steps.email.EmailPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.email.EmailTransformers;
import com.mulesoft.tools.migration.library.mule.steps.email.ImapInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.ImapsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.Pop3InboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.Pop3sInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.SmtpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.email.SmtpsOutboundEndpoint;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for email transports
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class EmailMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate EMail Components";
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
    return newArrayList(new EmailPomContribution(),
                        new ImapInboundEndpoint(),
                        new ImapsInboundEndpoint(),
                        new Pop3InboundEndpoint(),
                        new Pop3sInboundEndpoint(),
                        new SmtpOutboundEndpoint(),
                        new SmtpsOutboundEndpoint(),
                        new EmailTransformers(),
                        // The rest
                        new EmailConnectorConfig());
  }
}
