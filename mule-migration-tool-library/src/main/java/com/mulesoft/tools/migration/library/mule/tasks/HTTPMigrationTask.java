/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpBasicSecurity;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorHeaders;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListenerConfig;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorQueryParams;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequestConfig;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorUriParams;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpNamespaceContribution;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpPollingConnector;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpStaticResource;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpTransformers;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsPollingConnector;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsStaticResource;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for HTTP component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HTTPMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate HTTP Component";
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
    return newArrayList(new HttpConnectorPomContribution(),
                        new HttpNamespaceContribution(),
                        // Connector, introduced in Mule 3.6
                        new HttpConnectorListenerConfig(),
                        new HttpConnectorRequestConfig(),
                        new HttpConnectorListener(),
                        new HttpConnectorRequester(),
                        // Transport, deprecated in Mule 3.6
                        new HttpPollingConnector(),
                        new HttpsPollingConnector(),
                        new HttpGlobalEndpoint(),
                        new HttpsGlobalEndpoint(),
                        new HttpInboundEndpoint(),
                        new HttpsInboundEndpoint(),
                        new HttpOutboundEndpoint(),
                        new HttpsOutboundEndpoint(),
                        new HttpTransformers(),
                        // The rest
                        new HttpConnectorHeaders(),
                        new HttpConnectorQueryParams(),
                        new HttpConnectorUriParams(),
                        new HttpBasicSecurity(),
                        new HttpStaticResource(),
                        new HttpsStaticResource());
  }
}
