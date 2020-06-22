/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint.migrateFileFilters;
import static com.mulesoft.tools.migration.library.mule.steps.sftp.SftpConfig.SFTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateSchedulingStrategy;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateRedeliveryPolicyChildren;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Migrates the inbound endpoints of the sftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SftpInboundEndpoint extends AbstractSftpEndpoint {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri() = '" + SFTP_NS_URI + "' and local-name() = 'inbound-endpoint'][1]";

  @Override
  public String getDescription() {
    return "Update SFTP inbound endpoints.";
  }

  public SftpInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("listener");
    object.setNamespace(SFTP_NAMESPACE);
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    String configName = object.getAttributeValue("connector-ref");
    Optional<Element> config;
    if (configName != null) {
      config = getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + SFTP_NS_URI
          + "' and local-name() = 'config' and @name = '" + configName + "']");
    } else {
      config = getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + SFTP_NS_URI + "' and local-name() = 'config']");
    }

    Element sftpConfig = migrateSftpConfig(object, configName, config, report);
    Element connection = sftpConfig.getChild("connection", SFTP_NAMESPACE);

    addAttributesToInboundProperties(object, report);

    Element redelivery = object.getChild("idempotent-redelivery-policy", CORE_NAMESPACE);
    if (redelivery != null) {
      redelivery.setName("redelivery-policy");
      Attribute exprAttr = redelivery.getAttribute("idExpression");

      if (exprAttr != null) {
        // TODO MMT-128
        exprAttr.setValue(exprAttr.getValue().replaceAll("#\\[header\\:inbound\\:originalFilename\\]", "#[attributes.name]"));

        if (getExpressionMigrator().isWrapped(exprAttr.getValue())) {
          exprAttr.setValue(getExpressionMigrator()
              .wrap(getExpressionMigrator().migrateExpression(exprAttr.getValue(), true, object)));
        }
      }

      migrateRedeliveryPolicyChildren(redelivery, report);
    }

    migrateSchedulingStrategy(object, OptionalInt.of(1000));

    if (object.getAttribute("sizeCheckWaitTime") != null && !"0".equals(object.getAttributeValue("sizeCheckWaitTime"))) {
      String sizeCheckWaitTime = object.getAttributeValue("sizeCheckWaitTime");
      object.setAttribute("timeBetweenSizeCheck", sizeCheckWaitTime);
      object.removeAttribute("sizeCheckWaitTime");
    } else if (object.getAttribute("fileAge") != null && !"0".equals(object.getAttributeValue("fileAge"))) {
      String fileAge = object.getAttributeValue("fileAge");
      object.setAttribute("timeBetweenSizeCheck", fileAge);
      object.removeAttribute("fileAge");
    }

    if (object.getAttribute("tempDir") != null || object.getAttribute("useTempFileTimestampSuffix") != null) {
      report.report("sftp.tempDir", object, object);

      object.removeAttribute("tempDir");
      object.removeAttribute("useTempFileTimestampSuffix");
    }

    doExecute(object, report);

    migrateFileFilters(object, report, SFTP_NAMESPACE, getApplicationModel());

    processAddress(object, report).ifPresent(address -> {
      connection.setAttribute("host", address.getHost());
      connection.setAttribute("port", address.getPort());

      if (address.getCredentials() != null) {
        String[] credsSplit = address.getCredentials().split(":");

        connection.setAttribute("username", credsSplit[0]);

        if (credsSplit.length > 1) {
          connection.setAttribute("password", credsSplit[1]);
        }
      }
      object.setAttribute("path", address.getPath() != null ? resolveDirectory(address.getPath()) : "/");
    });
    copyAttributeIfPresent(object, connection, "host");
    copyAttributeIfPresent(object, connection, "port");
    copyAttributeIfPresent(object, connection, "user", "username");
    copyAttributeIfPresent(object, connection, "password");

    Attribute pathAttr = object.getAttribute("path");
    if (pathAttr != null) {
      pathAttr.setName("directory");
    }

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.setAttribute("config-ref", sftpConfig.getAttributeValue("name"));
    }
    object.removeAttribute("name");

    copyAttributeIfPresent(object, connection, "identityFile");
    copyAttributeIfPresent(object, connection, "passphrase");

    if (object.getAttribute("knownHostsFile") != null && connection.getAttribute("knownHostsFile") == null) {
      copyAttributeIfPresent(object, connection, "knownHostsFile");
    }
    object.removeAttribute("knownHostsFile");

    if (object.getAttribute("archiveDir") != null) {
      String fileArchiveConfigName = sftpConfig.getAttributeValue("name") + "Archive";
      addTopLevelElement(new Element("config", FILE_NAMESPACE)
          .setAttribute("name", fileArchiveConfigName)
          .addContent(new Element("connection", FILE_NAMESPACE)
              .setAttribute("workingDir", object.getAttributeValue("archiveDir"))), object.getDocument());

      object.getParentElement().addContent(3, new Element("write", FILE_NAMESPACE)
          .setAttribute("config-ref", fileArchiveConfigName)
          .setAttribute("path", "#[attributes.name]"));

      object.removeAttribute("archiveDir");
      object.removeAttribute("archiveTempReceivingDir");
      object.removeAttribute("archiveTempSendingDir");
    }

    if (object.getAttribute("responseTimeout") != null) {
      copyAttributeIfPresent(object, connection, "responseTimeout", "connectionTimeout");
      connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    }

    if (object.getAttribute("exchange-pattern") != null) {
      object.removeAttribute("exchange-pattern");
    }
  }

  protected Optional<Element> fetchConfig(String configName) {
    return getApplicationModel().getNodeOptional("/*/*[namespace-uri() = '" + SFTP_NS_URI + "' and local-name() = 'config']");
  }

  protected void doExecute(Element object, MigrationReport report) {
    // Nothing to do
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, true);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("originalFilename", "message.attributes.name");
    expressionsPerProperty.put("filename", "message.attributes.name");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.sftp.api.SftpFileAttributes", expressionsPerProperty);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
