/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.TLS_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;

/**
 * Migrates the inbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsInboundEndpoint extends HttpInboundEndpoint {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri() = '" + HTTPS_NAMESPACE_URI + "' and local-name() = 'inbound-endpoint' and position() = 1]";

  @Override
  public String getDescription() {
    return "Update HTTPs transport inbound endpoint.";
  }

  public HttpsInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

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

    handleHttpsListenerConfig(getApplicationModel(), object, report, httpsConnector);
  }

  public static void handleHttpsListenerConfig(ApplicationModel appModel, Element object, MigrationReport report,
                                               Element httpsConnector) {
    Element httpsListenerConnection =
        appModel.getNode("/*/*[namespace-uri() = '" + HTTP_NAMESPACE_URI + "' and local-name() = 'listener-config' and @name = '"
            + object.getAttributeValue("config-ref") + "']/*[namespace-uri() = '" + HTTP_NAMESPACE_URI
            + "' and local-name() = 'listener-connection']");

    httpsListenerConnection.setAttribute("protocol", "HTTPS");

    if (httpsConnector != null && httpsListenerConnection.getChild("context", TLS_NAMESPACE) == null) {
      Element tlsContext = new Element("context", TLS_NAMESPACE);
      boolean tlsConfigured = false;

      Element tlsServer = httpsConnector.getChild("tls-server", HTTPS_NAMESPACE);
      if (tlsServer != null) {
        Element trustStore = new Element("trust-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsServer, trustStore, "path");
        if (tlsServer.getAttribute("class") != null) {
          report.report("http.tlsServerClass", trustStore, tlsServer);
        }
        copyAttributeIfPresent(tlsServer, trustStore, "type", "type");
        copyAttributeIfPresent(tlsServer, trustStore, "storePassword", "password");
        copyAttributeIfPresent(tlsServer, trustStore, "algorithm");
        tlsContext.addContent(trustStore);
        tlsConfigured = true;
      }
      Element tlsKeyStore = httpsConnector.getChild("tls-key-store", HTTPS_NAMESPACE);
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

        httpsListenerConnection.addContent(tlsContext);
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
