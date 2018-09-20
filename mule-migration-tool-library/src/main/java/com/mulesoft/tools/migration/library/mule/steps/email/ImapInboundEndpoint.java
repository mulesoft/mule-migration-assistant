/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Migrates the imap inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ImapInboundEndpoint extends AbstractEmailMigrator
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/*/mule:flow/imap:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update IMap transport inbound endpoint.";
  }

  public ImapInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {

    object.setName("listener-imap");
    object.setNamespace(EMAIL_NAMESPACE);

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));
    addAttributesToInboundProperties(object, report);

    Optional<Element> imapConnector = resolveImapConnector(object, getApplicationModel());

    getApplicationModel().addNameSpace(EMAIL_NAMESPACE.getPrefix(), EMAIL_NAMESPACE.getURI(),
                                       "http://www.mulesoft.org/schema/mule/email/current/mule-email.xsd");

    imapConnector.ifPresent(c -> {
      if (c.getAttribute("moveToFolder") != null) {
        // TODO juani!
        report.report(ERROR, object, c, "'moveToFolder' is not yet supported in Email Connector");
      }

      if (c.getAttribute("deleteReadMessages") != null) {
        object.setAttribute("deleteAfterRetrieve", c.getAttributeValue("deleteReadMessages"));
      }

      if (c.getAttribute("checkFrequency") != null) {
        object.addContent(new Element("scheduling-strategy", CORE_NAMESPACE)
            .addContent(new Element("fixed-frequency", CORE_NAMESPACE)
                .setAttribute("frequency", c.getAttributeValue("checkFrequency"))));
      }
    });

    Element m4Config = migrateImapConfig(object, report, imapConnector, getApplicationModel());
    Element connection = m4Config.getChild("imap-connection", EMAIL_NAMESPACE);

    processAddress(object, report).ifPresent(address -> {
      connection.setAttribute("host", address.getHost());
      connection.setAttribute("port", address.getPort());

      if (address.getCredentials() != null) {
        String[] credsSplit = address.getCredentials().split(":");

        connection.setAttribute("user", credsSplit[0]);
        connection.setAttribute("password", credsSplit[1]);
      }
    });
    copyAttributeIfPresent(object, connection, "host");
    copyAttributeIfPresent(object, connection, "port");
    copyAttributeIfPresent(object, connection, "user");
    copyAttributeIfPresent(object, connection, "password");

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.removeAttribute("name");
      object.setAttribute("config-ref", m4Config.getAttributeValue("name"));
    }

//    object.setAttribute("config-ref", m4Config.getName());

    // connector.ifPresent(m3c -> {
    // Element reconnectforever = m3c.getChild("reconnect-forever", CORE_NAMESPACE);
    // if (reconnectforever != null) {
    // object.addContent(new Element("reconnect-forever", CORE_NAMESPACE)
    // .setAttribute("frequency", reconnectforever.getAttributeValue("frequency")));
    // }
    //
    // Element reconnect = m3c.getChild("reconnect", CORE_NAMESPACE);
    // if (reconnect != null) {
    // object.addContent(new Element("reconnect", CORE_NAMESPACE)
    // .setAttribute("frequency", reconnect.getAttributeValue("frequency"))
    // .setAttribute("count", reconnect.getAttributeValue("count")));
    // }
    //
    // if (m3c.getAttributeValue("acknowledgementMode") != null) {
    // switch (m3c.getAttributeValue("acknowledgementMode")) {
    // case "CLIENT_ACKNOWLEDGE":
    // object.setAttribute("ackMode", "MANUAL");
    // break;
    // case "DUPS_OK_ACKNOWLEDGE":
    // object.setAttribute("ackMode", "DUPS_OK");
    // break;
    // default:
    // // AUTO is default, no need to set it
    // }
    // }
    //
    // if (m3c.getAttributeValue("numberOfConsumers") != null) {
    // object.setAttribute("numberOfConsumers", m3c.getAttributeValue("numberOfConsumers"));
    // }
    //
    // handleConnectorChildElements(m3c, new Element("connection", CORE_NAMESPACE), report);
    // });
    //

  }

  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/imap:connector[@name = '" + connectorName + "']");
  }

  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional("/*/imap:connector");
  }

  protected Optional<Element> resolveImapConnector(Element object, ApplicationModel appModel) {
    Optional<Element> connector;
    if (object.getAttribute("connector-ref") != null) {
      connector = of(getConnector(object.getAttributeValue("connector-ref")));
      object.removeAttribute("connector-ref");
    } else {
      connector = getDefaultConnector();
    }
    return connector;
  }

  public static Element migrateImapConfig(Element object, MigrationReport report, Optional<Element> connector,
                                         ApplicationModel appModel) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "ImapConfig");

    Optional<Element> config = appModel.getNodeOptional("*/email:imap-config[@name='" + configName + "']");
    return config.orElseGet(() -> {
      final Element imapCfg = new Element("imap-config", EMAIL_NAMESPACE);
      imapCfg.setAttribute("name", configName);

      Element connection = new Element("imap-connection", EMAIL_NAMESPACE);
      imapCfg.addContent(connection);

      addTopLevelElement(imapCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return imapCfg;
    });
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, false);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    // expressionsPerProperty.put("originalFilename", "message.attributes.name");
    // expressionsPerProperty.put("fileSize", "message.attributes.size");
    // expressionsPerProperty.put("timestamp", "message.attributes.timestamp");
    //
    // try {
    // addAttributesMapping(getApplicationModel(), "org.mule.extension.ftp.api.ftp.FtpFileAttributes", expressionsPerProperty);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
