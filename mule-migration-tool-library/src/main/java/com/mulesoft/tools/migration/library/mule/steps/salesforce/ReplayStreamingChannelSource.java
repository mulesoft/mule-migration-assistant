/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.library.tools.SalesforceUtils;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import org.jdom2.Element;

/**
 * Migrate Replay Streaming Channel Source
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ReplayStreamingChannelSource extends AbstractSalesforceSourcesMigrationStep implements ExpressionMigratorAware {

  private static String m3Name = "replay-streaming-channel";
  private static String m4Name = "replay-channel-listener";

  public ReplayStreamingChannelSource() {
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

    String replayOption = mule3Source.getAttributeValue("replayOption");
    if (replayOption != null && !replayOption.isEmpty()) {
      mule4Source.setAttribute("replayOption", replayOption);
    }

    String replayId = mule3Source.getAttributeValue("replayId");
    if (replayId != null && !replayId.isEmpty()) {
      mule4Source.setAttribute("replayId", replayId);
    }

    String autoReplay = mule3Source.getAttributeValue("autoReplay");
    if (autoReplay != null && !autoReplay.isEmpty()) {
      mule4Source.setAttribute("autoReplay", autoReplay);
    }

    String bufferSizeInBytes = mule3Source.getAttributeValue("bufferSizeInBytes");
    if (bufferSizeInBytes != null && !bufferSizeInBytes.isEmpty()) {
      mule4Source.setAttribute("bufferSizeInBytes", bufferSizeInBytes);
    }
  }
}
