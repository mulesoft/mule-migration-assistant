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
    return newArrayList(
    // new HttpConnectorPomContribution(),
    // // Connector, introduced in Mule 3.6
    // new HttpConnectorListenerConfig(),
    // new HttpConnectorRequestConfig(),
    // new HttpConnectorListener(),
    // new HttpConnectorRequester(),
    // // Transport, deprecated in Mule 3.6
    // new HttpPollingConnector(),
    // new HttpsPollingConnector(),
    // new HttpGlobalEndpoint(),
    // new HttpsGlobalEndpoint(),
    // new HttpInboundEndpoint(),
    // new HttpsInboundEndpoint(),
    // new HttpOutboundEndpoint(),
    // new HttpsOutboundEndpoint(),
    // new HttpTransformers(),
    // // The rest
    // new HttpConnectorHeaders(),
    // new HttpConnectorQueryParams(),
    // new HttpConnectorUriParams(),
    // new HttpBasicSecurity(),
    // new HttpStaticResource(),
    // new HttpsStaticResource()
    );
  }
}
