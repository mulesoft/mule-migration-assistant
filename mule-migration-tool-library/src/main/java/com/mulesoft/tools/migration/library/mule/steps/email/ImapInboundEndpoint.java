/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateSchedulingStrategy;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnection;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Migrates the imap inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ImapInboundEndpoint extends AbstractEmailSourceMigrator implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + IMAP_NAMESPACE_URI + "' and local-name()='inbound-endpoint'][1]";

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

    migrateSchedulingStrategy(object, OptionalInt.empty());
    Element fixedFrequency = object.getChild("scheduling-strategy", CORE_NAMESPACE).getChild("fixed-frequency", CORE_NAMESPACE);

    imapConnector.ifPresent(c -> {
      handleServiceOverrides(c, report);
      migrateReconnection(c, object, report);

      if (c.getAttribute("moveToFolder") != null) {
        // TODO https://www.mulesoft.org/jira/browse/MULE-15721
        report.report("email.moveToFolder", c, object);
      }

      if (c.getAttribute("mailboxFolder") != null) {
        object.setAttribute("folder", c.getAttributeValue("mailboxFolder"));
      }
      if (c.getAttribute("backupEnabled") != null || c.getAttribute("backupFolder") != null) {
        report.report("email.imapBackup", c, object);
      }

      if (c.getAttribute("deleteReadMessages") != null) {
        object.setAttribute("deleteAfterRetrieve", c.getAttributeValue("deleteReadMessages"));
      }
      if (c.getAttribute("defaultProcessMessageAction") != null) {
        object.removeAttribute("defaultProcessMessageAction");
        report.report("email.imapDefaultProcessMessageAction", c, object);
      }

      if (c.getAttribute("checkFrequency") != null) {
        fixedFrequency.setAttribute("frequency", c.getAttributeValue("checkFrequency"));
      }
    });

    Element m4Config = migrateImapConfig(object, report, imapConnector);
    Element connection = getConnection(m4Config);

    if (imapConnector.isPresent() && "gmail-connector".equals(imapConnector.get().getName())) {
      connection.setName("imaps-connection");
      connection.addContent(new Element("context", TLS_NAMESPACE)
          .addContent(new Element("trust-store", TLS_NAMESPACE).setAttribute("insecure", "true")));

      connection.setAttribute("host", "imap.gmail.com");
      connection.setAttribute("port", "993");
      object.removeAttribute("host");
      object.removeAttribute("port");

      getApplicationModel().addNameSpace(TLS_NAMESPACE.getPrefix(), TLS_NAMESPACE.getURI(),
                                         "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

      report.report("email.gmail", imapConnector.get(), connection);
    } else {
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
    }

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
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + IMAP_NAMESPACE_URI
        + "' and (local-name()='connector' or local-name()='gmail-connector') and @name = '" + connectorName + "']");
  }

  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("imap-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + IMAP_NAMESPACE_URI
            + "' and (local-name()='connector' or local-name()='gmail-connector')]");
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
