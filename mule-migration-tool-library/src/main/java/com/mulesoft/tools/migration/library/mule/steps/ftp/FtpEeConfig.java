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

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.stream.Stream;

/**
 * Migrates the ftp connector of the fto-ee transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpEeConfig extends FtpConfig {

  private static final String FTP_EE_NAMESPACE_PREFIX = "ftp-ee";
  private static final String FTP_EE_NS_URI = "http://www.mulesoft.org/schema/mule/ee/ftp";
  public static final Namespace FTP_EE_NAMESPACE = Namespace.getNamespace(FTP_EE_NAMESPACE_PREFIX, FTP_EE_NS_URI);
  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + FTP_EE_NS_URI + "' and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Update FTP-ee connector config.";
  }

  public FtpEeConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(FTP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    super.execute(object, report);

    String fileAge = null;
    if (object.getAttribute("fileAge") != null) {
      fileAge = object.getAttributeValue("fileAge");
      object.removeAttribute("fileAge");
    }

    handleInputSpecificAttributes(object, fileAge, report);
  }

  private void handleInputSpecificAttributes(Element object, String fileAge, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//*[namespace-uri() = '" + FTP_EE_NS_URI + "' and local-name() = 'inbound-endpoint' and @connector-ref='"
            + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToInboundEnpoint(object, fileAge, e));

    object.removeAttribute("moveToDirectory");
    object.removeAttribute("moveToPattern");
  }

  private void passConnectorConfigToInboundEnpoint(Element object, String fileAge, Element listener) {
    if (fileAge != null && !"0".equals(fileAge)) {
      listener.setAttribute("timeBetweenSizeCheck", fileAge);
    }

    if (object.getAttribute("moveToDirectory") != null && listener.getAttribute("moveToDirectory") == null) {
      listener.setAttribute("moveToDirectory", object.getAttributeValue("moveToDirectory"));
    }

    if (object.getAttribute("moveToPattern") != null) {
      String moveToPattern = object.getAttributeValue("moveToPattern");
      listener.setAttribute("renameTo",
                            getExpressionMigrator().migrateExpression(moveToPattern, true, listener));
    }
  }
}
