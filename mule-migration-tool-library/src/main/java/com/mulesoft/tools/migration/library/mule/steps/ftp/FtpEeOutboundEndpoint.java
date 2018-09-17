/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

/**
 * Migrates the outbound endpoints of the ftp-ee transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpEeOutboundEndpoint extends FtpOutboundEndpoint {

  public static final String XPATH_SELECTOR = "//ftp-ee:outbound-endpoint";

  @Override
  public String getDescription() {
    return "Update FTP-ee outbound endpoints.";
  }

  public FtpEeOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

}
