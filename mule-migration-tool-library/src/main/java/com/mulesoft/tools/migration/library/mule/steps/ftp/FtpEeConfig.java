/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
  private static final Namespace FTP_EE_NAMESPACE = Namespace.getNamespace(FTP_EE_NAMESPACE_PREFIX, FTP_EE_NS_URI);
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
        .getNodes("//ftp:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToInboundEnpoint(object, fileAge, e));

    // object.removeAttribute("pollingFrequency");
    // object.removeAttribute("readFromDirectory");
    // object.removeAttribute("moveToDirectory");
    // object.removeAttribute("autoDelete");
    // object.removeAttribute("recursive");
    // object.removeAttribute("moveToDirectory");
    // object.removeAttribute("moveToPattern");
  }

  private void passConnectorConfigToInboundEnpoint(Element object, String fileAge, Element listener) {
    // Element schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
    // listener.addContent(schedulingStr);
    // Element fixedFrequency = new Element("fixed-frequency", CORE_NAMESPACE);
    // fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
    // schedulingStr.addContent(fixedFrequency);

    // if (object.getAttribute("readFromDirectory") != null) {
    // listener.setAttribute("directory", object.getAttributeValue("readFromDirectory"));
    // }
    if (fileAge != null && !"0".equals(fileAge)) {
      listener.setAttribute("timeBetweenSizeCheck", fileAge);
    }
    //
    // String autoDelete = changeDefault("true", "false", object.getAttributeValue("autoDelete"));
    // if (autoDelete != null) {
    // listener.setAttribute("autoDelete", autoDelete);
    // }
    //
    // String recursive = changeDefault("false", "true", object.getAttributeValue("recursive"));
    // listener.setAttribute("recursive", recursive != null ? recursive : "true");
    //
    // if (object.getAttribute("moveToDirectory") != null && listener.getAttribute("moveToDirectory") == null) {
    // listener.setAttribute("moveToDirectory", object.getAttributeValue("moveToDirectory"));
    // }
    //
    // if (object.getAttribute("moveToPattern") != null) {
    // String moveToPattern = object.getAttributeValue("moveToPattern");
    // listener.setAttribute("renameTo",
    // getExpressionMigrator().migrateExpression(moveToPattern, true, listener));
    // }
    //
    // if (matcherUsed) {
    // listener.setAttribute("matcher", object.getAttributeValue("name") + "Matcher");
    // }
  }
}
