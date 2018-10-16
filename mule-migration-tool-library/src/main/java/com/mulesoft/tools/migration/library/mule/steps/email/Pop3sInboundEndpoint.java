/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
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
 * Migrates the pop3s inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Pop3sInboundEndpoint extends Pop3InboundEndpoint {

  public static final String XPATH_SELECTOR = "/*/mule:flow/pop3s:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Pop3s transport inbound endpoint.";
  }

  public Pop3sInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Optional<Element> pop3sConnector;
    if (object.getAttribute("connector-ref") != null) {
      pop3sConnector = Optional.of(getConnector(object.getAttributeValue("connector-ref")));
    } else {
      pop3sConnector = getDefaultConnector();
    }

    super.execute(object, report);

    Element pop3sConnection = getApplicationModel()
        .getNode("/*/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'pop3-config' and @name = '"
            + object.getAttributeValue("config-ref")
            + "']/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'pop3s-connection']");

    Namespace tlsNamespace = Namespace.getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

    if (pop3sConnector.isPresent() && pop3sConnection.getChild("context", tlsNamespace) == null) {

      Element tlsContext = new Element("context", tlsNamespace);
      boolean tlsConfigured = false;

      Namespace pop3sNamespace = getNamespace("pop3s", "http://www.mulesoft.org/schema/mule/pop3s");
      Element tlsKeyStore = pop3sConnector.get().getChild("tls-client", pop3sNamespace);
      if (tlsKeyStore != null) {
        Element keyStore = new Element("key-store", tlsNamespace);
        copyAttributeIfPresent(tlsKeyStore, keyStore, "path");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "storePassword", "password");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyPassword");
        if (tlsKeyStore.getAttribute("class") != null) {
          report.report("email.pop3KeyStoreClass", tlsKeyStore, tlsKeyStore);
        }
        copyAttributeIfPresent(tlsKeyStore, keyStore, "type", "type");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyAlias", "alias");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "algorithm");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }
      Element tlsClient = pop3sConnector.get().getChild("tls-trust-store", pop3sNamespace);
      if (tlsClient != null) {
        Element keyStore = new Element("trust-store", tlsNamespace);
        copyAttributeIfPresent(tlsClient, keyStore, "path");
        copyAttributeIfPresent(tlsClient, keyStore, "storePassword", "password");
        if (tlsClient.getAttribute("class") != null) {
          report.report("email.pop3TlsClientClass", tlsClient, tlsClient);
        }
        copyAttributeIfPresent(tlsClient, keyStore, "type", "type");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }

      if (tlsConfigured) {
        getApplicationModel().addNameSpace(tlsNamespace.getPrefix(), tlsNamespace.getURI(),
                                           "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

        pop3sConnection.addContent(tlsContext);
      }
    }
  }

  @Override
  protected Element createConnection() {
    return new Element("pop3s-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("pop3s-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/pop3s:connector[@name = '" + connectorName + "']");
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional("/*/pop3s:connector");
  }
}
