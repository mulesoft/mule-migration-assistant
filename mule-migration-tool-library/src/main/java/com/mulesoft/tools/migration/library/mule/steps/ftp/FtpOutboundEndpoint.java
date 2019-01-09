/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig.FTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.extractInboundChildren;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.io.IOException;
import java.util.Optional;

/**
 * Migrates the outbound endpoints of the ftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpOutboundEndpoint extends AbstractFtpEndpoint {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + FTP_NS_URI + "' and local-name() = 'outbound-endpoint']";

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
    object.setNamespace(FTP_NAMESPACE);

    String configName = object.getAttributeValue("connector-ref");
    Optional<Element> config;
    if (configName != null) {
      config = getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + FTP_NS_URI
          + "' and local-name() = 'config' and @name = '" + configName + "']");
    } else {
      config = getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + FTP_NS_URI + "' and local-name() = 'config']");
    }

    Element ftpConfig = migrateFtpConfig(object, config, configName, config, report);
    Element connection = ftpConfig.getChild("connection", FTP_NAMESPACE);

    processAddress(object, report).ifPresent(address -> {
      connection.setAttribute("host", address.getHost());
      connection.setAttribute("port", address.getPort());

      if (address.getCredentials() != null) {
        String[] credsSplit = address.getCredentials().split(":");

        connection.setAttribute("username", credsSplit[0]);
        connection.setAttribute("password", credsSplit[1]);
      }
      object.setAttribute("path", address.getPath() != null ? address.getPath() : "/");
    });
    copyAttributeIfPresent(object, connection, "host");
    copyAttributeIfPresent(object, connection, "port");
    copyAttributeIfPresent(object, connection, "user", "username");
    copyAttributeIfPresent(object, connection, "password");

    Attribute pathAttr = object.getAttribute("path");
    if (pathAttr != null) {
      pathAttr.setValue(resolveDirectory(pathAttr.getValue()));
      // pathAttr.setName("directory");
    }
    copyAttributeIfPresent(object, connection, "path", "workingDir");

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.setAttribute("config-ref", ftpConfig.getAttributeValue("name"));
    }
    object.removeAttribute("name");

    if (object.getAttribute("responseTimeout") != null) {
      copyAttributeIfPresent(object, connection, "responseTimeout", "connectionTimeout");
      connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    }

    extractInboundChildren(object, getApplicationModel());

    migrateOperationStructure(getApplicationModel(), object, report);

    object.setAttribute("path", compatibilityOutputFile("{"
        + " outputPattern: " + propToDwExpr(object, "outputPattern") + ","
        + " outputPatternConfig: " + getExpressionMigrator().unwrap(propToDwExpr(object, "outputPatternConfig"))
        + "}"));

    if (object.getAttribute("exchange-pattern") != null) {
      object.removeAttribute("exchange-pattern");
    }
  }

  private String compatibilityOutputFile(String pathDslParams) {
    try {
      // Replicates logic from org.mule.transport.ftp.FtpConnector.getFilename(ImmutableEndpoint, MuleMessage)
      library(getMigrationScriptFolder(getApplicationModel().getProjectBasePath()), "FtpWriteOutputFile.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the outbound endpoint logic for determining the output filename of the Mule 3.x Ftp transport."
                  + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun ftpWriteOutputfile(vars: {}, pathDslParams: {}) = do {" + lineSeparator() +
                  "    ((((vars.compatibility_outboundProperties.filename" + lineSeparator() +
                  "        default pathDslParams.outputPattern)" + lineSeparator() +
                  "        default vars.compatibility_outboundProperties.outputPattern)" + lineSeparator() +
                  "        default pathDslParams.outputPatternConfig)" + lineSeparator() +
                  "        default (uuid() ++ '.dat'))" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return "#[migration::FtpWriteOutputFile::ftpWriteOutputfile(vars, " + pathDslParams + ")]";
  }

  private String propToDwExpr(Element object, String propName) {
    if (object.getAttribute(propName) != null) {
      if (getExpressionMigrator().isTemplate(object.getAttributeValue(propName))) {
        return getExpressionMigrator()
            .unwrap(getExpressionMigrator().migrateExpression(object.getAttributeValue(propName), true, object));
      } else {
        return "'" + object.getAttributeValue(propName) + "'";
      }
    } else {
      return "null";
    }
  }
}
