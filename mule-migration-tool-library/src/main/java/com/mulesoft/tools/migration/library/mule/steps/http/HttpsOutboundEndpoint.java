/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the outbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsOutboundEndpoint extends HttpOutboundEndpoint {

  private static final String HTTP_NS_PREFIX = "http";
  private static final String HTTP_NS_URI = "http://www.mulesoft.org/schema/mule/http";
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
    Optional<Element> httpsConnector;
    if (object.getAttribute("connector-ref") != null) {
      httpsConnector = Optional.of(getConnector(object.getAttributeValue("connector-ref")));
    } else {
      httpsConnector = getDefaultConnector();
    }

    super.execute(object, report);
    getApplicationModel().addNameSpace(HTTP_NS_PREFIX, HTTP_NS_URI,
                                       "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");

    Element httpsRequesterConnection = getApplicationModel().getNode("/mule:mule/http:request-config[@name = '"
        + object.getAttributeValue("config-ref") + "']/http:request-connection");

    migrate(httpsRequesterConnection, httpsConnector, report, getApplicationModel(), "tls-client");
  }

  public static void migrate(Element httpsRequesterConnection, Optional<Element> httpsConnector, MigrationReport report,
                             ApplicationModel appModel, String tlsClientTagName) {
    Namespace httpsNamespace = Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https");
    Namespace tlsNamespace = Namespace.getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

    httpsRequesterConnection.setAttribute("protocol", "HTTPS");

    if (httpsConnector.isPresent() && httpsRequesterConnection.getChild("context", tlsNamespace) == null) {
      Element tlsContext = new Element("context", tlsNamespace);
      boolean tlsConfigured = false;

      Element tlsClient = httpsConnector.get().getChild(tlsClientTagName, httpsNamespace);
      if (tlsClient != null) {
        Element keyStore = new Element("trust-store", tlsNamespace);
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
      Element tlsKeyStore = httpsConnector.get().getChild("tls-key-store", httpsNamespace);
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
        appModel.addNameSpace(tlsNamespace.getPrefix(), tlsNamespace.getURI(),
                              "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

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
    return getApplicationModel().getNodeOptional("/mule:mule/https:connector");
  }

}
