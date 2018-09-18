/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

/**
 * Migrates the inbound endpoints of the ftp-ee transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpEeInboundEndpoint extends FtpInboundEndpoint {

  // private static final String FTP_NS_PREFIX = "ftp-ee";
  private static final String FTP_EE_NS_URI = "http://www.mulesoft.org/schema/mule/ee/ftp";
  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri() = '" + FTP_EE_NS_URI + "' and local-name() = 'inbound-endpoint'][1]";


  // namespace-uri() = '" + FTP_EE_NS_URI + "' and local-name() = 'commit'
  @Override
  public String getDescription() {
    return "Update FTP-ee inbound endpoints.";
  }

  public FtpEeInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

}
