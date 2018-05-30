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

import java.util.List;
import java.util.Optional;

/**
 * Migrates the outbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsOutboundEndpoint extends HttpOutboundEndpoint {

  public static final String XPATH_SELECTOR =
      "/mule:mule//https:outbound-endpoint";

  @Override
  public String getDescription() {
    return "Update HTTPs transport outbound endpoint.";
  }

  public HttpsOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace httpsNamespace = Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https");
    Namespace tlsNamespace = Namespace.getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

    Optional<Element> httpsConnector;
    if (object.getAttribute("ref") != null) {
      httpsConnector = Optional.of(getConnector(object.getAttributeValue("ref")));
    } else {
      httpsConnector = getDefaultConnector();
    }

    super.execute(object, report);

    Element httpsRequesterConnection = getApplicationModel().getNode("/mule:mule/http:request-config[@name = '"
        + object.getAttributeValue("config-ref") + "']/http:request-connection");

    migrate(httpsRequesterConnection, httpsConnector, report, httpsNamespace, tlsNamespace);
  }

  public void migrate(Element httpsRequesterConnection, Optional<Element> httpsConnector, MigrationReport report,
                      Namespace httpsNamespace, Namespace tlsNamespace) {
    httpsRequesterConnection.setAttribute("protocol", "HTTPS");

    if (httpsConnector.isPresent()) {
      Element tlsContext = new Element("context", tlsNamespace);
      boolean tlsConfigured = false;

      Element tlsServer = httpsConnector.get().getChild("tls-server", httpsNamespace);
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
      Element tlsClient = httpsConnector.get().getChild("tls-client", httpsNamespace);
      if (tlsClient != null) {
        Element keyStore = new Element("key-store", tlsNamespace);
        copyAttributeIfPresent(tlsClient, keyStore, "path");
        copyAttributeIfPresent(tlsClient, keyStore, "storePassword", "password");
        if (tlsClient.getAttribute("class") != null) {
          report.report(ERROR, tlsClient, tlsClient,
                        "'class' attribute of 'https:tls-client' was deprecated in 3.x. Use 'type' instead.");
        }
        copyAttributeIfPresent(tlsClient, keyStore, "type", "type");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }

      if (tlsConfigured) {
        httpsRequesterConnection.addContent(tlsContext);
      }
    }
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/mule:mule/https:connector[@name = '" + connectorName + "']");
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    List<Element> nodes = getApplicationModel().getNodes("/mule:mule/https:connector");
    return nodes.stream().findFirst();
  }

}
