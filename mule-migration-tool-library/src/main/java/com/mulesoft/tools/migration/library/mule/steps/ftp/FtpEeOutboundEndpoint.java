/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

/**
 * Migrates the outbound endpoints of the ftp-ee transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpEeOutboundEndpoint extends FtpOutboundEndpoint {

  private static final String FTP_EE_NS_URI = "http://www.mulesoft.org/schema/mule/ee/ftp";
  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + FTP_EE_NS_URI + "' and local-name() = 'outbound-endpoint']";

  @Override
  public String getDescription() {
    return "Update FTP-ee outbound endpoints.";
  }

  public FtpEeOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

}
