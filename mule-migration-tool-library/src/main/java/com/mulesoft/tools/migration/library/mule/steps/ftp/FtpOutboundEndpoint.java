/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig.FTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.extractInboundChildren;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the outbound endpoints of the ftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpOutboundEndpoint extends AbstractFtpEndpoint {

  public static final String XPATH_SELECTOR = "//ftp:outbound-endpoint";

  @Override
  public String getDescription() {
    return "Update FTP outbound endpoints.";
  }

  public FtpOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("write");

    String configName = object.getAttributeValue("connector-ref");
    Optional<Element> config =
        getApplicationModel().getNodeOptional("/*/ftp:config[@name = '" + configName + "']");

    Element ftpConfig = migrateFtpConfig(object, configName, config);
    Element connection = ftpConfig.getChild("connection", FTP_NAMESPACE);

    copyAttributeIfPresent(object, connection, "host");
    copyAttributeIfPresent(object, connection, "port");
    copyAttributeIfPresent(object, connection, "user", "username");
    copyAttributeIfPresent(object, connection, "password");

    if (object.getAttribute("responseTimeout") != null) {
      copyAttributeIfPresent(object, connection, "responseTimeout", "connectionTimeout");
      connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    }

    object.getAttribute("connector-ref").setName("config-ref");

    extractInboundChildren(object, getApplicationModel());

    migrateOperationStructure(getApplicationModel(), object, report);

    // object.setAttribute("path", compatibilityOutputFile("{"
    // + " writeToDirectory: "
    // + (object.getAttribute("path") == null ? propToDwExpr(object, "writeToDirectory")
    // : "'" + object.getAttributeValue("path") + "'")
    // + ","
    // + " address: "
    // + (object.getAttribute("address") != null
    // ? ("'" + object.getAttributeValue("address").substring("file://".length()) + "'")
    // : "null")
    // + ","
    // + " outputPattern: " + propToDwExpr(object, "outputPattern") + ","
    // + " outputPatternConfig: " + getExpressionMigrator().unwrap(propToDwExpr(object, "outputPatternConfig"))
    // + "}"));
    //
    // if (object.getAttribute("connector-ref") != null) {
    // object.getAttribute("connector-ref").setName("config-ref");
    // }
    //
    // if (object.getAttribute("outputAppend") != null && !"false".equals(object.getAttributeValue("outputAppend"))) {
    // object.setAttribute("mode", "APPEND");
    // }
    //
    // object.removeAttribute("writeToDirectory");
    // object.removeAttribute("outputPattern");
    // object.removeAttribute("outputPatternConfig");
    // object.removeAttribute("outputAppend");
    //
    // if (object.getAttribute("name") != null) {
    // object.removeAttribute("name");
    // }
  }

  // private String compatibilityOutputFile(String pathDslParams) {
  // try {
  // // Replicates logic from org.mule.transport.file.FileConnector.getOutputStream(OutboundEndpoint, MuleEvent)
  // library(getMigrationScriptFolder(getApplicationModel().getProjectBasePath()), "FileWriteOutputFile.dwl",
  // "" +
  // "/**" + lineSeparator() +
  // " * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x File transport."
  // + lineSeparator() +
  // " */" + lineSeparator() +
  // "fun fileWriteOutputfile(vars: {}, pathDslParams: {}) = do {" + lineSeparator() +
  // " ((vars.compatibility_outboundProperties['writeToDirectoryName']" + lineSeparator() +
  // " default pathDslParams.writeToDirectory)" + lineSeparator() +
  // " default pathDslParams.address)" + lineSeparator() +
  // " ++ '/' ++" + lineSeparator() +
  // " ((((pathDslParams.outputPattern" + lineSeparator() +
  // " default vars.compatibility_outboundProperties.outputPattern)" + lineSeparator() +
  // " default pathDslParams.outputPatternConfig)" + lineSeparator() +
  // " default vars.compatibility_inboundProperties.filename)" + lineSeparator() +
  // " default (uuid() ++ '.dat'))" + lineSeparator() +
  // "}" + lineSeparator() +
  // lineSeparator());
  // } catch (IOException e) {
  // throw new RuntimeException(e);
  // }
  //
  // return "#[migration::FileWriteOutputFile::fileWriteOutputfile(vars, " + pathDslParams + ")]";
  // }
  //
  // private String propToDwExpr(Element object, String propName) {
  // if (object.getAttribute(propName) != null) {
  // if (getExpressionMigrator().isWrapped(object.getAttributeValue(propName))) {
  // return getExpressionMigrator().migrateExpression(object.getAttributeValue(propName), true, object);
  // } else {
  // return "'" + object.getAttributeValue(propName) + "'";
  // }
  // } else {
  // return "null";
  // }
  // }

}
