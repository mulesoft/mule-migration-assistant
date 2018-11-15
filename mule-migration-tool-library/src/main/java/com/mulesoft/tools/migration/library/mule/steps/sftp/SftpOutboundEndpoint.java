/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.library.mule.steps.sftp.SftpConfig.SFTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.extractInboundChildren;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the outbound endpoints of the sftp transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SftpOutboundEndpoint extends AbstractSftpEndpoint {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + SFTP_NS_URI + "' and local-name() = 'outbound-endpoint']";

  @Override
  public String getDescription() {
    return "Update SFTP outbound endpoints.";
  }

  public SftpOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("write");
    object.setNamespace(SFTP_NAMESPACE);

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

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.setAttribute("config-ref", sftpConfig.getAttributeValue("name"));
    }
    object.removeAttribute("name");

    // if (object.getAttribute("responseTimeout") != null) {
    // copyAttributeIfPresent(object, connection, "responseTimeout", "connectionTimeout");
    // connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    // }

    extractInboundChildren(object, getApplicationModel());

    migrateOperationStructure(getApplicationModel(), object, report);
  }

}
