/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the inbound endpoints of the ftp-ee transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpEeInboundEndpoint extends FtpInboundEndpoint {

  private static final String FTP_EE_NS_URI = "http://www.mulesoft.org/schema/mule/ee/ftp";
  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri() = '" + FTP_EE_NS_URI + "' and local-name() = 'inbound-endpoint'][1]";

  @Override
  public String getDescription() {
    return "Update FTP-ee inbound endpoints.";
  }

  public FtpEeInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  protected void doExecute(Element object, MigrationReport report) {
    super.doExecute(object, report);

    if (object.getAttribute("fileAge") != null && !"0".equals(object.getAttributeValue("fileAge"))) {
      String fileAge = object.getAttributeValue("fileAge");
      object.setAttribute("timeBetweenSizeCheck", fileAge);
      object.removeAttribute("fileAge");
    }

    if (object.getAttribute("moveToPattern") != null) {
      String moveToPattern = object.getAttributeValue("moveToPattern");
      object.setAttribute("renameTo",
                          getExpressionMigrator().migrateExpression(moveToPattern, true, object));
      object.removeAttribute("moveToPattern");
    }

    // TODO test
    if (object.getAttribute("moveToDirectory") != null) {
      if ("true".equals(object.getAttributeValue("autoDelete"))) {
        object.removeAttribute("autoDelete");
      }
    }

  }

}
