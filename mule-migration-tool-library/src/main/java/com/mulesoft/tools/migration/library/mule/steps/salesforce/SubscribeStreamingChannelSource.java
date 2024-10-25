/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.Element;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Migrate Subscribe Streaming Channel Source
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SubscribeStreamingChannelSource extends AbstractSalesforceSourcesMigrationStep implements ExpressionMigratorAware {

  private static String m3Name = "subscribe-streaming-channel";
  private static String m4Name = "subscribe-channel-listener";

  public SubscribeStreamingChannelSource() {
    super(m3Name, m4Name);
    this.setAppliedTo(XmlDslUtils.getXPathSelector(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE_URI, m3Name, false));
    this.setNamespacesContributions(newArrayList(SalesforceUtils.MULE3_SALESFORCE_NAMESPACE));
  }

  @Override
  public void execute(Element mule3Source, MigrationReport report) throws RuntimeException {
    super.execute(mule3Source, report);
    resolveAttributes(mule3Source, mule4Source);

    XmlDslUtils.addElementAfter(mule4Source, mule3Source);
    mule3Source.getParentElement().removeContent(mule3Source);

  }

  protected void resolveAttributes(Element mule3Source, Element mule4Source) {
    String streamingChannel = mule3Source.getAttributeValue("streamingChannel");
    if (streamingChannel != null && !streamingChannel.isEmpty()) {
      mule4Source.setAttribute("streamingChannel", streamingChannel);
    }
  }
}
