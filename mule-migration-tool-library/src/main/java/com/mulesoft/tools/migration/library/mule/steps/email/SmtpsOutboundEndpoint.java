/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the outbound smtps endpoint of the email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SmtpsOutboundEndpoint extends SmtpOutboundEndpoint {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + SMTPS_NAMESPACE_URI + "' and local-name()='outbound-endpoint']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update SMTPs transport outbound endpoint.";
  }

  public SmtpsOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Optional<Element> smtpsConnector;
    if (object.getAttribute("connector-ref") != null) {
      smtpsConnector = Optional.of(getConnector(object.getAttributeValue("connector-ref")));
    } else {
      smtpsConnector = getDefaultConnector();
    }

    super.execute(object, report);

    Element smtpsConnection = getApplicationModel()
        .getNode("/*/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'smtp-config' and @name = '"
            + object.getAttributeValue("config-ref")
            + "']/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'smtps-connection']");

    if (smtpsConnector.isPresent() && smtpsConnection.getChild("context", TLS_NAMESPACE) == null) {

      Element tlsContext = new Element("context", TLS_NAMESPACE);
      boolean tlsConfigured = false;

      Namespace smtpsNamespace = getNamespace("smtps", "http://www.mulesoft.org/schema/mule/smtps");
      Element tlsKeyStore = smtpsConnector.get().getChild("tls-client", smtpsNamespace);
      if (tlsKeyStore != null) {
        Element keyStore = new Element("key-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsKeyStore, keyStore, "path");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "storePassword", "password");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyPassword");
        if (tlsKeyStore.getAttribute("class") != null) {
          report.report("email.smtpKeyStoreClass", tlsKeyStore, tlsKeyStore);
        }
        copyAttributeIfPresent(tlsKeyStore, keyStore, "type", "type");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyAlias", "alias");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "algorithm");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }
      Element tlsClient = smtpsConnector.get().getChild("tls-trust-store", smtpsNamespace);
      if (tlsClient != null) {
        Element keyStore = new Element("trust-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsClient, keyStore, "path");
        copyAttributeIfPresent(tlsClient, keyStore, "storePassword", "password");
        if (tlsClient.getAttribute("class") != null) {
          report.report("email.smtpTlsClientClass", tlsClient, tlsClient);
        }
        copyAttributeIfPresent(tlsClient, keyStore, "type", "type");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }

      if (tlsConfigured) {
        getApplicationModel().addNameSpace(TLS_NAMESPACE.getPrefix(), TLS_NAMESPACE.getURI(),
                                           "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

        smtpsConnection.addContent(tlsContext);
      }
    }
  }

  @Override
  protected Element createConnection() {
    return new Element("smtps-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("smtps-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + SMTPS_NAMESPACE_URI
        + "' and (local-name()='connector' or local-name()='gmail-connector') and @name = '" + connectorName + "']");
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + SMTPS_NAMESPACE_URI
            + "' and (local-name()='connector' or local-name()='gmail-connector')]");
  }
}
