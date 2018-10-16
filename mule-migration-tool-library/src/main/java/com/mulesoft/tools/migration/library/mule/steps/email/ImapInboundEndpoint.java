/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the imap inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ImapInboundEndpoint extends AbstractEmailSourceMigrator implements ExpressionMigratorAware {

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

    Optional<Element> imapConnector = resolveConnector(object, getApplicationModel());

    getApplicationModel().addNameSpace(EMAIL_NAMESPACE.getPrefix(), EMAIL_NAMESPACE.getURI(), EMAIL_SCHEMA_LOC);

    Element fixedFrequency = new Element("fixed-frequency", CORE_NAMESPACE);
    object.addContent(new Element("scheduling-strategy", CORE_NAMESPACE).addContent(fixedFrequency));

    imapConnector.ifPresent(c -> {
      if (c.getAttribute("moveToFolder") != null) {
        // TODO https://www.mulesoft.org/jira/browse/MULE-15721
        report.report("email.moveToFolder", object, c);
      }

      if (c.getAttribute("mailboxFolder") != null) {
        object.setAttribute("folder", c.getAttributeValue("mailboxFolder"));
      }
      if (c.getAttribute("backupEnabled") != null || c.getAttribute("backupFolder") != null) {
        report.report("email.imapBackup", object, c);
      }

      if (c.getAttribute("deleteReadMessages") != null) {
        object.setAttribute("deleteAfterRetrieve", c.getAttributeValue("deleteReadMessages"));
      }
      if (c.getAttribute("defaultProcessMessageAction") != null) {
        object.removeAttribute("defaultProcessMessageAction");
        report.report("email.imapDefaultProcessMessageAction", object, c);
      }

      if (c.getAttribute("checkFrequency") != null) {
        fixedFrequency.setAttribute("frequency", c.getAttributeValue("checkFrequency"));
      }
    });

    Element m4Config = migrateImapConfig(object, report, imapConnector);
    Element connection = getConnection(m4Config);

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


    if (object.getAttribute("responseTimeout") != null) {
      connection.setAttribute("readTimeout", object.getAttributeValue("responseTimeout"));
      connection.setAttribute("writeTimeout", object.getAttributeValue("responseTimeout"));
      connection.setAttribute("timeoutUnit", "MILLISECONDS");
      object.removeAttribute("responseTimeout");
    }
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/imap:connector[@name = '" + connectorName + "']");
  }

  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("imap-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional("/*/imap:connector");
  }

  public Element migrateImapConfig(Element object, MigrationReport report, Optional<Element> connector) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "ImapConfig");

    Optional<Element> config = getApplicationModel()
        .getNodeOptional("*/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'imap-config' and @name='"
            + configName + "']");
    return config.orElseGet(() -> {
      final Element imapCfg = new Element("imap-config", EMAIL_NAMESPACE);
      imapCfg.setAttribute("name", configName);

      Element connection = createConnection();
      imapCfg.addContent(connection);

      addTopLevelElement(imapCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return imapCfg;
    });
  }

  protected Element createConnection() {
    return new Element("imap-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected String getInboundAttributesClass() {
    return "org.mule.extension.email.api.attributes.IMAPEmailAttributes";
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
