/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator.VARS_OUTBOUND_PREFIX;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnection;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.io.IOException;
import java.util.Optional;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Text;

/**
 * Migrates the outbound smtp endpoint of the email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SmtpOutboundEndpoint extends AbstractEmailMigrator
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + SMTP_NAMESPACE_URI + "' and local-name()='outbound-endpoint']";

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

    smtpConnector.ifPresent(c -> {
      handleServiceOverrides(c, report);
      migrateReconnection(c, object, report);

      if (c.getAttribute("toAddresses") != null) {
        object.setAttribute("tc", c.getAttributeValue("toAddresses"));
      }
      if (c.getAttribute("ccAddresses") != null) {
        object.setAttribute("cc", c.getAttributeValue("ccAddresses"));
      }
      if (c.getAttribute("bccAddresses") != null) {
        object.setAttribute("bcc", c.getAttributeValue("bccAddresses"));
      }

      if (c.getAttribute("fromAddress") != null) {
        object.setAttribute("fromAddress", c.getAttributeValue("fromAddress"));
      }
      if (c.getAttribute("replyToAddresses") != null) {
        object.setAttribute("replyTo", c.getAttributeValue("replyToAddresses"));
      }
      if (c.getAttribute("subject") != null) {
        object.setAttribute("subject", c.getAttributeValue("subject"));
      }
    });

    getApplicationModel().addNameSpace(EMAIL_NAMESPACE.getPrefix(), EMAIL_NAMESPACE.getURI(), EMAIL_SCHEMA_LOC);

    Element m4Config = migrateSmtpConfig(object, report, smtpConnector);
    Element connection = getConnection(m4Config);

    if (smtpConnector.isPresent() && "gmail-connector".equals(smtpConnector.get().getName())) {
      connection.setName("smtps-connection");
      connection.addContent(new Element("context", TLS_NAMESPACE)
          .addContent(new Element("trust-store", TLS_NAMESPACE).setAttribute("insecure", "true")));

      connection.setAttribute("host", "smtp.gmail.com");
      connection.setAttribute("port", "465");
      object.removeAttribute("host");
      object.removeAttribute("port");

      getApplicationModel().addNameSpace(TLS_NAMESPACE.getPrefix(), TLS_NAMESPACE.getURI(),
                                         "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

      report.report("email.gmail", smtpConnector.get(), connection);
    } else {
      if (object.getAttribute("host") != null) {
        object.setAttribute("host", expressionMigrator.migrateExpression(object.getAttributeValue("host"), false, object));
      }
      copyAttributeIfPresent(object, connection, "host");
      copyAttributeIfPresent(object, connection, "port");

      processAddress(object, report).ifPresent(address -> {
        connection.setAttribute("host", address.getHost());
        connection.setAttribute("port", address.getPort());

        if (address.getCredentials() != null) {
          String[] credsSplit = address.getCredentials().split(":");

          connection.setAttribute("user", credsSplit[0]);
          connection.setAttribute("password", credsSplit[1]);
        }
      });
    }

    copyAttributeIfPresent(object, connection, "user");
    copyAttributeIfPresent(object, connection, "password");

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.removeAttribute("name");
      object.setAttribute("config-ref", m4Config.getAttributeValue("name"));
    }

    report.report("email.outbound", object, object);
    object.setAttribute("fromAddress",
                        smtpAttributeExpr("#[migration::SmtpTransport::smtpFromAddress(vars)]",
                                          object.getAttribute("fromAddress")));
    object.setAttribute("subject",
                        smtpAttributeExpr("#[migration::SmtpTransport::smtpSubject(vars)]", object.getAttribute("subject")));
    object.addContent(new Element("to-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("to-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smtpToAddress(vars)]", object.getAttribute("to")))));
    object.addContent(new Element("cc-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("cc-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smtpCcAddress(vars)]", object.getAttribute("cc")))));
    object.addContent(new Element("bcc-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("bcc-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smtpBccAddress(vars)]", object.getAttribute("bcc")))));
    object.addContent(new Element("reply-to-addresses", EMAIL_NAMESPACE)
        .addContent(new Element("reply-to-address", EMAIL_NAMESPACE)
            .setAttribute("value",
                          smtpAttributeExpr("#[migration::SmtpTransport::smtpReplyToAddress(vars)]",
                                            object.getAttribute("replyTo")))));
    object.addContent(new Element("headers", EMAIL_NAMESPACE)
        .addContent(new Text("#[migration::SmtpTransport::smtpCustomHeaders(vars)]")));
    object.addContent(new Element("body", EMAIL_NAMESPACE)
        .setAttribute("contentType", "#[payload.^mimeType]")
        .addContent(new Element("content", EMAIL_NAMESPACE).addContent(new Text("#[payload]"))));
    object.addContent(new Element("attachments", EMAIL_NAMESPACE)
        .addContent(new Text("#[vars filterObject ((value,key) -> ((key as String) startsWith 'att_')) pluck ((value, key, index) -> value)]")));

    object.removeAttribute("to");
    object.removeAttribute("cc");
    object.removeAttribute("bcc");
    object.removeAttribute("replyTo");

    migrateOutboundEndpointStructure(getApplicationModel(), object, report, false);
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
        .getNodeOptional("*/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'smtp-config' and @name='"
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
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + SMTP_NAMESPACE_URI
        + "' and (local-name()='connector' or local-name()='gmail-connector') and @name = '" + connectorName + "']");
  }

  protected Element createConnection() {
    return new Element("smtp-connection", EMAIL_NAMESPACE);
  }

  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("smtp-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + SMTP_NAMESPACE_URI
            + "' and (local-name()='connector' or local-name()='gmail-connector')]");
  }

  public static void smtpTransportLib(ApplicationModel appModel) {
    try {
      String varPrefix = appModel.noCompatibilityMode() ? VARS_OUTBOUND_PREFIX : "vars.compatibility_outboundProperties.";
      // Replicates logic from org.mule.transport.email.transformers.StringToEmailMessage
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "SmtpTransport.dwl",
              "" +
                  "fun smtpToAddress(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "toAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smtpCcAddress(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "ccAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smtpBccAddress(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "bccAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smtpFromAddress(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "fromAddress" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smtpReplyToAddress(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "replyToAddresses[0]" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smtpSubject(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "subject" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "fun smtpCustomHeaders(vars: {}) = do {" + lineSeparator() +
                  "    " + varPrefix + "customHeaders" + lineSeparator() +
                  "}" + lineSeparator());
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
