/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConnectorPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeConfig;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpEeOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpInboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpNamespaceHandler;
import com.mulesoft.tools.migration.library.mule.steps.ftp.FtpOutboundEndpoint;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for FTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate FTP Transport";
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
    return newArrayList(new FtpConnectorPomContribution(),
                        new FtpGlobalEndpoint(), new FtpEeGlobalEndpoint(),
                        new FtpConfig(), new FtpEeConfig(),
                        new FtpInboundEndpoint(), new FtpEeInboundEndpoint(),
                        new FtpOutboundEndpoint(), new FtpEeOutboundEndpoint(),
                        new FtpNamespaceHandler());
  }
}
