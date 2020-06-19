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
