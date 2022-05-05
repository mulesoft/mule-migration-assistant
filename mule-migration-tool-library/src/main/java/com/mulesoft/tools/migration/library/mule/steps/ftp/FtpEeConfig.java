/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
    this.setNamespacesContributions(newArrayList(FTP_EE_NAMESPACE));
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
