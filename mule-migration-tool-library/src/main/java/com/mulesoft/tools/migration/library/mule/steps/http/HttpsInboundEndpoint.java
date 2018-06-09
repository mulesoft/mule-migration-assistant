/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsInboundEndpoint extends HttpInboundEndpoint {

  private static final String HTTP_NS_PREFIX = "http";
  private static final String HTTP_NS_URI = "http://www.mulesoft.org/schema/mule/http";
  public static final String XPATH_SELECTOR =
      "/mule:mule/mule:flow/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/https' and local-name() = 'inbound-endpoint' and position() = 1]";

  @Override
  public String getDescription() {
    return "Update HTTPs transport inbound endpoint.";
  }

  public HttpsInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace httpsNamespace = Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https");
    Namespace tlsNamespace = Namespace.getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

    Element httpsConnector = null;
    if (object.getAttribute("connector-ref") != null) {
      httpsConnector = getConnector(object.getAttributeValue("connector-ref"));
    } else {
      Optional<Element> defaultConnector = getDefaultConnector();
      if (defaultConnector.isPresent()) {
        httpsConnector = defaultConnector.get();
      }
    }

    super.execute(object, report);

    getApplicationModel().addNameSpace(HTTP_NS_PREFIX, HTTP_NS_URI,
                                       "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");

    Element httpsListenerConnection = getApplicationModel().getNode("/mule:mule/http:listener-config[@name = '"
        + object.getAttributeValue("config-ref") + "']/http:listener-connection");

    httpsListenerConnection.setAttribute("protocol", "HTTPS");

    if (httpsConnector != null && httpsListenerConnection.getChild("context", tlsNamespace) == null) {
      Element tlsContext = new Element("context", tlsNamespace);
      boolean tlsConfigured = false;

      Element tlsServer = httpsConnector.getChild("tls-server", httpsNamespace);
      if (tlsServer != null) {
        Element trustStore = new Element("trust-store", tlsNamespace);
        copyAttributeIfPresent(tlsServer, trustStore, "path");
        if (tlsServer.getAttribute("class") != null) {
          report.report(ERROR, trustStore, tlsServer,
                        "'class' attribute of 'https:tls-server' was deprecated in 3.x. Use 'type' instead.");
        }
        copyAttributeIfPresent(tlsServer, trustStore, "type", "type");
        copyAttributeIfPresent(tlsServer, trustStore, "storePassword", "password");
        copyAttributeIfPresent(tlsServer, trustStore, "algorithm");
        tlsContext.addContent(trustStore);
        tlsConfigured = true;
      }
      Element tlsKeyStore = httpsConnector.getChild("tls-key-store", httpsNamespace);
      if (tlsKeyStore != null) {
        Element keyStore = new Element("key-store", tlsNamespace);
        copyAttributeIfPresent(tlsKeyStore, keyStore, "path");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "storePassword", "password");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyPassword");
        if (tlsKeyStore.getAttribute("class") != null) {
          report.report(ERROR, tlsKeyStore, tlsKeyStore,
                        "'class' attribute of 'https:tls-key-store' was deprecated in 3.x. Use 'type' instead.");
        }
        copyAttributeIfPresent(tlsKeyStore, keyStore, "type", "type");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyAlias", "alias");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "algorithm");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }

      if (tlsConfigured) {
        getApplicationModel().addNameSpace(tlsNamespace.getPrefix(), tlsNamespace.getURI(),
                                           "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

        httpsListenerConnection.addContent(tlsContext);
      }
    }
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/mule:mule/https:connector[@name = '" + connectorName + "']");
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional("/mule:mule/https:connector");
  }

}
