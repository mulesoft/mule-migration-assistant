/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.TLS_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the outbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsOutboundEndpoint extends HttpOutboundEndpoint {

  private static final String HTTP_NS_PREFIX = "http";
  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + HTTPS_NAMESPACE_URI + "' and local-name()='outbound-endpoint']";

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
    getApplicationModel().addNameSpace(HTTP_NS_PREFIX, HTTPS_NAMESPACE_URI,
                                       "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");

    Element httpsRequesterConnection = getApplicationModel()
        .getNode("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='request-config' and @name = '"
            + object.getAttributeValue("config-ref") + "']/*[namespace-uri()='" + HTTP_NAMESPACE_URI
            + "' and local-name()='request-connection']");

    migrate(httpsRequesterConnection, httpsConnector, report, getApplicationModel(), "tls-client");
  }

  public static void migrate(Element httpsRequesterConnection, Optional<Element> httpsConnector, MigrationReport report,
                             ApplicationModel appModel, String tlsClientTagName) {
    httpsRequesterConnection.setAttribute("protocol", "HTTPS");

    if (httpsConnector.isPresent() && httpsRequesterConnection.getChild("context", TLS_NAMESPACE) == null) {
      Element tlsContext = new Element("context", TLS_NAMESPACE);
      boolean tlsConfigured = false;

      Element tlsClient = httpsConnector.get().getChild(tlsClientTagName, HTTPS_NAMESPACE);
      if (tlsClient != null) {
        Element keyStore = new Element("trust-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsClient, keyStore, "path");
        copyAttributeIfPresent(tlsClient, keyStore, "storePassword", "password");
        if (tlsClient.getAttribute("class") != null) {
          report.report("http.tlsClientClass", tlsClient, tlsClient);
        }
        copyAttributeIfPresent(tlsClient, keyStore, "type", "type");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }
      Element tlsKeyStore = httpsConnector.get().getChild("tls-key-store", HTTPS_NAMESPACE);
      if (tlsKeyStore != null) {
        Element keyStore = new Element("key-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsKeyStore, keyStore, "path");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "storePassword", "password");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyPassword");
        if (tlsKeyStore.getAttribute("class") != null) {
          report.report("http.tlsKeyStoreClass", tlsKeyStore, tlsKeyStore);
        }
        copyAttributeIfPresent(tlsKeyStore, keyStore, "type", "type");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyAlias", "alias");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "algorithm");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }

      if (tlsConfigured) {
        appModel.addNameSpace(TLS_NAMESPACE.getPrefix(), TLS_NAMESPACE.getURI(),
                              "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

        httpsRequesterConnection.addContent(tlsContext);
      }
    }
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + HTTPS_NAMESPACE_URI
        + "' and local-name()='connector' and @name = '" + connectorName + "']");
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + HTTPS_NAMESPACE_URI + "' and local-name()='connector']");
  }

}
