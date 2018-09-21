/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
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
 * Migrates the Pop3 inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Pop3InboundEndpoint extends AbstractEmailSourceMigrator implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/*/mule:flow/pop3:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Pop3 transport inbound endpoint.";
  }

  public Pop3InboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("listener-pop3");
    object.setNamespace(EMAIL_NAMESPACE);

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));
    addAttributesToInboundProperties(object, report);

    Optional<Element> pop3Connector = resolveConnector(object, getApplicationModel());

    getApplicationModel().addNameSpace(EMAIL_NAMESPACE.getPrefix(), EMAIL_NAMESPACE.getURI(),
                                       "http://www.mulesoft.org/schema/mule/email/current/mule-email.xsd");

    pop3Connector.ifPresent(c -> {
      if (c.getAttribute("moveToFolder") != null) {
        // TODO https://www.mulesoft.org/jira/browse/MULE-15721
        report.report(ERROR, object, c, "'moveToFolder' is not yet supported in Email Connector");
      }

      if (c.getAttribute("mailboxFolder") != null) {
        object.setAttribute("folder", c.getAttributeValue("mailboxFolder"));
      }
      if (c.getAttribute("backupEnabled") != null || c.getAttribute("backupFolder") != null) {
        report.report(ERROR, object, c, "'backupEnabled' and 'backupFolder' are no longer supported in Email Connector");
      }

      if (c.getAttribute("deleteReadMessages") != null) {
        object.setAttribute("deleteAfterRetrieve", c.getAttributeValue("deleteReadMessages"));
      }
      if (c.getAttribute("defaultProcessMessageAction") != null) {
        object.removeAttribute("defaultProcessMessageAction");
        report.report(ERROR, object, c, "'defaultProcessMessageAction' is no longer supported in Email Connector");
      }

      if (c.getAttribute("checkFrequency") != null) {
        object.addContent(new Element("scheduling-strategy", CORE_NAMESPACE)
            .addContent(new Element("fixed-frequency", CORE_NAMESPACE)
                .setAttribute("frequency", c.getAttributeValue("checkFrequency"))));
      }
    });

    Element m4Config = migratePop3Config(object, report, pop3Connector);
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
    return getApplicationModel().getNode("/*/pop3:connector[@name = '" + connectorName + "']");
  }

  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("pop3-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional("/*/pop3:connector");
  }

  public Element migratePop3Config(Element object, MigrationReport report, Optional<Element> connector) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "Pop3Config");

    Optional<Element> config = getApplicationModel()
        .getNodeOptional("*/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/email' and local-name() = 'pop3-config' and @name='"
            + configName + "']");
    return config.orElseGet(() -> {
      final Element imapCfg = new Element("pop3-config", EMAIL_NAMESPACE);
      imapCfg.setAttribute("name", configName);

      Element connection = createConnection();
      imapCfg.addContent(connection);

      addTopLevelElement(imapCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return imapCfg;
    });
  }

  protected Element createConnection() {
    return new Element("pop3-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected String getInboundAttributesClass() {
    return "org.mule.extension.email.api.attributes.POP3EmailAttributes";
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
