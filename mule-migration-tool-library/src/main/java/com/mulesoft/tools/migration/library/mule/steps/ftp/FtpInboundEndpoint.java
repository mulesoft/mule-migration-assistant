/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.library.mule.steps.file.FileInboundEndpoint.migrateFileFilters;
import static com.mulesoft.tools.migration.library.mule.steps.ftp.FtpConfig.FTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the inbound endpoints of the ftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpInboundEndpoint extends AbstractFtpEndpoint {

  public static final String XPATH_SELECTOR = "/*/mule:flow/ftp:inbound-endpoint[1]";

  @Override
  public String getDescription() {
    return "Update FTP inbound endpoints.";
  }

  public FtpInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("listener");
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    String configName = object.getAttributeValue("connector-ref");
    Optional<Element> config =
        getApplicationModel().getNodeOptional("/*/ftp:config[@name = '" + configName + "']");

    Element ftpConfig = migrateFtpConfig(object, configName, config);
    Element connection = ftpConfig.getChild("connection", FTP_NAMESPACE);

    addAttributesToInboundProperties(object, report);

    // Element redelivery = object.getChild("idempotent-redelivery-policy", CORE_NAMESPACE);
    // if (redelivery != null) {
    // redelivery.setName("redelivery-policy");
    // Attribute exprAttr = redelivery.getAttribute("idExpression");
    //
    // // TODO MMT-128
    // exprAttr.setValue(exprAttr.getValue().replaceAll("#\\[header\\:inbound\\:originalFilename\\]", "#[attributes.name]"));
    //
    // if (getExpressionMigrator().isWrapped(exprAttr.getValue())) {
    // exprAttr
    // .setValue(getExpressionMigrator().wrap(getExpressionMigrator().migrateExpression(exprAttr.getValue(), true, object)));
    // }
    // }

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

    // if (object.getAttribute("fileAge") != null && !"0".equals(object.getAttributeValue("fileAge"))) {
    // String fileAge = object.getAttributeValue("fileAge");
    // object.setAttribute("timeBetweenSizeCheck", fileAge);
    // object.removeAttribute("fileAge");
    // }
    //
    // if (object.getAttribute("moveToPattern") != null) {
    // String moveToPattern = object.getAttributeValue("moveToPattern");
    // object.setAttribute("renameTo",
    // getExpressionMigrator().migrateExpression(moveToPattern, true, object));
    // object.removeAttribute("moveToPattern");
    // }
    //
    // // TODO test
    // if (object.getAttribute("moveToDirectory") != null) {
    // if ("true".equals(object.getAttributeValue("autoDelete"))) {
    // object.removeAttribute("autoDelete");
    // }
    // }

    migrateFileFilters(object, report, FTP_NAMESPACE, getApplicationModel());

    // object.setAttribute("applyPostActionWhenFailed", "false");
    //
    // String recursive = changeDefault("false", "true", object.getAttributeValue("recursive"));
    // if (recursive != null) {
    // object.setAttribute("recursive", recursive);
    // } else {
    // object.removeAttribute("recursive");
    // }
    //
    processAddress(object, report).ifPresent(address -> {
      connection.setAttribute("host", address.getHost());
      connection.setAttribute("port", address.getPort());

      if (address.getCredentials() != null) {
        String[] credsSplit = address.getCredentials().split(":");

        connection.setAttribute("username", credsSplit[0]);
        connection.setAttribute("password", credsSplit[1]);
      }
      object.setAttribute("directory", address.getPath() != null ? address.getPath() : "/");
    });
    copyAttributeIfPresent(object, connection, "host");
    copyAttributeIfPresent(object, connection, "port");
    copyAttributeIfPresent(object, connection, "user", "username");
    copyAttributeIfPresent(object, connection, "password");

    if (object.getAttribute("path") != null) {
      object.getAttribute("path").setName("directory");
    }
    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.removeAttribute("name");
      object.setAttribute("config-ref", ftpConfig.getAttributeValue("name"));

      // Set the Mule 3 defaults since those are different in Mule 4
      // object.setAttribute("autoDelete", "true");
      // object.setAttribute("recursive", "false");
    }

    copyAttributeIfPresent(object, connection, "passive");
    if (object.getAttribute("binary") != null) {
      connection.setAttribute("transferMode", "true".equals(object.getAttributeValue("binary")) ? "BINARY" : "ASCII");
      object.removeAttribute("binary");
    }

    if (object.getAttribute("encoding") != null) {
      object.getParent().addContent(3, new Element("set-payload", CORE_NAMESPACE)
          .setAttribute("value", "#[payload]")
          .setAttribute("encoding", object.getAttributeValue("encoding")));
      object.removeAttribute("encoding");
    }
    if (object.getAttribute("responseTimeout") != null) {
      copyAttributeIfPresent(object, connection, "responseTimeout", "connectionTimeout");
      connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    }

    // if (object.getAttribute("comparator") != null || object.getAttribute("reverseOrder") != null) {
    // report.report(ERROR, object, object,
    // "'comparator'/'reverseOrder' are not yet supported by the file connector listener.",
    // "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-file#file_listener");
    // object.removeAttribute("comparator");
    // object.removeAttribute("reverseOrder");
    // }
    //
    // if (object.getAttribute("name") != null) {
    // object.removeAttribute("name");
    // }
  }

  // private Element buildNewMatcher(Element object, Namespace fileNs) {
  // Element newMatcher;
  // newMatcher = new Element("matcher", fileNs);
  //
  // List<Element> referencedMatcher =
  // getApplicationModel().getNodes("/*/file:matcher[@name='" + object.getAttributeValue("matcher") + "']");
  // if (!referencedMatcher.isEmpty()) {
  // for (Attribute attribute : referencedMatcher.get(0).getAttributes()) {
  // newMatcher.setAttribute(attribute.getName(), attribute.getValue());
  // }
  // }
  //
  // String newMatcherName =
  // (object.getAttributeValue("connector-ref") != null ? object.getAttributeValue("connector-ref") + "-" : "")
  // + object.getParentElement().getAttributeValue("name") + "Matcher";
  // newMatcher.setAttribute("name", newMatcherName);
  // object.setAttribute("matcher", newMatcherName);
  //
  // int idx = object.getDocument().getRootElement().indexOf(object.getParentElement());
  // object.getDocument().getRootElement().addContent(idx, newMatcher);
  // return newMatcher;
  // }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, true);

    // Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    // expressionsPerProperty.put("originalFilename", "message.attributes.fileName");
    // expressionsPerProperty.put("originalDirectory",
    // "(message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))]");
    // expressionsPerProperty.put("sourceFileName", "message.attributes.fileName");
    // expressionsPerProperty.put("sourceDirectory",
    // "(message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))]");
    // expressionsPerProperty.put("filename", "message.attributes.fileName");
    // expressionsPerProperty.put("directory",
    // "(message.attributes.path as String) [0 to -(2 + sizeOf(message.attributes.fileName))]");
    // expressionsPerProperty.put("fileSize", "message.attributes.size");
    // expressionsPerProperty.put("timestamp", "message.attributes.lastModifiedTime");
    // expressionsPerProperty.put("MULE.FORCE_SYNC", "false");
    //
    // try {
    // addAttributesMapping(getApplicationModel(), "org.mule.extension.file.api.LocalFileAttributes", expressionsPerProperty);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
  }

}
