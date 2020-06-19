/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
