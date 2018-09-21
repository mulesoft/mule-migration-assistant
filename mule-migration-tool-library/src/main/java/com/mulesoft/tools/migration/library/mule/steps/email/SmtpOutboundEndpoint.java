/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Text;

import java.io.IOException;
import java.util.Optional;

/**
 * Migrates the outbound smtp endpoint of the email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SmtpOutboundEndpoint extends AbstractEmailMigrator
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "//smtp:outbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update SMTP transport outbound endpoint.";
  }

  public SmtpOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    smtpTransportLib(getApplicationModel());

    object.setName("send");
    object.setNamespace(EMAIL_NAMESPACE);

    Optional<Element> smtpConnector = resolveConnector(object, getApplicationModel());

    getApplicationModel().addNameSpace(EMAIL_NAMESPACE.getPrefix(), EMAIL_NAMESPACE.getURI(),
                                       "http://www.mulesoft.org/schema/mule/email/current/mule-email.xsd");

    Element m4Config = migrateSmtpConfig(object, report, smtpConnector);
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

    object.setAttribute("host", expressionMigrator.migrateExpression(object.getAttributeValue("host"), false, object));
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

    report.report(ERROR, object, object,
                  "Remove any unneeded children and add any missing ones, based on the properties set prevous to this operation.");
    object.setAttribute("subject",
                        smtpAttributeExpr("#[vars.compatibility_outboundProperties.subject]", object.getAttribute("subject")));
    object.addContent(new Element("to-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("to-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smptToAddress(vars)]", object.getAttribute("to")))));
    object.addContent(new Element("cc-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("cc-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smptCcAddress(vars)]", object.getAttribute("cc")))));
    object.addContent(new Element("bcc-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("bcc-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smptBccAddress(vars)]", object.getAttribute("bcc")))));
    object.addContent(new Element("reply-to-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("reply-to-address", EMAIL_NAMESPACE)
            .setAttribute("value", smtpAttributeExpr("#[migration::SmtpTransport::smptReplyToAddress(vars)]",
                                                     object.getAttribute("replyTo")))));
    object.addContent(new Element("headers", EMAIL_NAMESPACE)
        .addContent(new Text("#[vars.compatibility_outboundProperties.customHeaders]")));
    object.addContent(new Element("body", EMAIL_NAMESPACE)
        .setAttribute("contentType", "#[payload.^mimeType]")
        .addContent(new Element("content", EMAIL_NAMESPACE).addContent(new Text("#[payload]"))));

    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true);
  }


  private String smtpAttributeExpr(String propsBase, Attribute endpointAttr) {
    if (endpointAttr == null) {
      return propsBase;
    } else {
      String original = endpointAttr.getValue();

      String defaultValue = expressionMigrator.isWrapped(original)
          ? expressionMigrator.unwrap(expressionMigrator.migrateExpression(original, true, endpointAttr.getParent()))
          : "'" + original + "'";

      String newExpr = expressionMigrator.wrap(expressionMigrator.unwrap(propsBase) + " default " + defaultValue);
      endpointAttr.detach();
      return newExpr;
    }
  }

  public Element migrateSmtpConfig(Element object, MigrationReport report, Optional<Element> connector) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "SmtpConfig");

    Optional<Element> config = getApplicationModel()
        .getNodeOptional("*/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/email' and local-name() = 'smtp-config' and @name='"
            + configName + "']");
    return config.orElseGet(() -> {
      final Element smtpCfg = new Element("smtp-config", EMAIL_NAMESPACE);
      smtpCfg.setAttribute("name", configName);

      Element connection = createConnection();
      smtpCfg.addContent(connection);

      addTopLevelElement(smtpCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return smtpCfg;
    });
  }


  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/smtp:connector[@name = '" + connectorName + "']");
  }

  protected Element createConnection() {
    return new Element("smtp-connection", EMAIL_NAMESPACE);
  }

  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("smtp-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional("/*/smtp:connector");
  }

  public static void smtpTransportLib(ApplicationModel appModel) {
    try {
      // Replicates logic from org.mule.transport.email.transformers.StringToEmailMessage
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "SmtpTransport.dwl",
              "" +
                  "fun smptToAddress(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.toAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smptCcAddress(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.ccAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smptBccAddress(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.bccAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smptFromAddress(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.fromAddress" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smptReplyToAddress(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.replyToAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smptSubject(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.subject" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
