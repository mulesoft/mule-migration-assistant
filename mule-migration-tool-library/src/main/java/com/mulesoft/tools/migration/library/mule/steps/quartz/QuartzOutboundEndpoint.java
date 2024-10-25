/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.quartz;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Removes the outbound endpoints of the quartz transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QuartzOutboundEndpoint extends AbstractApplicationModelMigrationStep {

  protected static final String QUARTZ_NS_PREFIX = "quartz";
  protected static final String QUARTZ_NS_URI = "http://www.mulesoft.org/schema/mule/quartz";
  private static final Namespace QUARTZ_NS = getNamespace(QUARTZ_NS_PREFIX, QUARTZ_NS_URI);

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + QUARTZ_NS_URI + "' and local-name() = 'outbound-endpoint']";

  @Override
  public String getDescription() {
    return "Remove quartz outbound endpoints.";
  }

  public QuartzOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("quartz.outboundEndpoint", object, object.getParentElement());

    Element endpointPollingJob = object.getChild("scheduled-dispatch-job", QUARTZ_NS);
    if (endpointPollingJob != null) {
      Element jobEndpoint = endpointPollingJob.getChild("job-endpoint", QUARTZ_NS);

      jobEndpoint.detach();
      jobEndpoint.setName("outbound-endpoint");
      jobEndpoint.setNamespace(CORE_NAMESPACE);

      addElementAfter(jobEndpoint, object);
      object.detach();
    }
  }

}
