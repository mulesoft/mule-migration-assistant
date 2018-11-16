/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint.migrateFileFilters;
import static com.mulesoft.tools.migration.library.mule.steps.sftp.SftpConfig.SFTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateRedeliveryPolicyChildren;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

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

    Element sftpConfig = migrateSftpConfig(object, configName, config);
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

    Element schedulingStr = object.getChild("scheduling-strategy", CORE_NAMESPACE);
    if (schedulingStr == null) {
      schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
      schedulingStr.addContent(new Element("fixed-frequency", CORE_NAMESPACE));
      object.addContent(schedulingStr);
    }

    Element fixedFrequency = schedulingStr.getChild("fixed-frequency", CORE_NAMESPACE);

    if (object.getAttribute("pollingFrequency") != null) {
      fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
    } else if (fixedFrequency.getAttribute("frequency") == null) {
      fixedFrequency.setAttribute("frequency", "1000");
    }
    object.removeAttribute("pollingFrequency");

    doExecute(object, report);

    migrateFileFilters(object, report, SFTP_NAMESPACE, getApplicationModel());

    processAddress(object, report).ifPresent(address -> {
      connection.setAttribute("host", address.getHost());
      connection.setAttribute("port", address.getPort());

      if (address.getCredentials() != null) {
        String[] credsSplit = address.getCredentials().split(":");

        connection.setAttribute("username", credsSplit[0]);
        connection.setAttribute("password", credsSplit[1]);
      }
      object.setAttribute("path", address.getPath() != null ? resolveDirectory(address.getPath()) : "/");
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
      object.setAttribute("config-ref", sftpConfig.getAttributeValue("name"));
    }
    object.removeAttribute("name");

    // copyAttributeIfPresent(object, connection, "passive");
    // if (object.getAttribute("binary") != null) {
    // connection.setAttribute("transferMode", "true".equals(object.getAttributeValue("binary")) ? "BINARY" : "ASCII");
    // object.removeAttribute("binary");
    // }
    //
    // if (object.getAttribute("encoding") != null) {
    // object.getParent().addContent(3, new Element("set-payload", CORE_NAMESPACE)
    // .setAttribute("value", "#[payload]")
    // .setAttribute("encoding", object.getAttributeValue("encoding")));
    // object.removeAttribute("encoding");
    // }

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

    // Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    // expressionsPerProperty.put("originalFilename", "message.attributes.name");
    // expressionsPerProperty.put("fileSize", "message.attributes.size");
    // expressionsPerProperty.put("timestamp", "message.attributes.timestamp");
    //
    // try {
    // addAttributesMapping(getApplicationModel(), "org.mule.extension.sftp.api.SftpFileAttributes", expressionsPerProperty);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
  }

}
