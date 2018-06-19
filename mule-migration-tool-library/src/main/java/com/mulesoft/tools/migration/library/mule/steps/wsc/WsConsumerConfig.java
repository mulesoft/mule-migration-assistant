/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.wsc;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint.handleConnector;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint.migrate;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Migrates the configuration of the WebService consumer config
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class WsConsumerConfig extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule/ws:consumer-config";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update WebService consumer config.";
  }

  public WsConsumerConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace wscNamespace = Namespace.getNamespace("wsc", "http://www.mulesoft.org/schema/mule/wsc");
    getApplicationModel().addNameSpace(wscNamespace.getPrefix(), wscNamespace.getURI(),
                                       "http://www.mulesoft.org/schema/mule/wsc/current/mule-wsc.xsd");
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(wscNamespace);
    object.setName("config");

    Element connection = new Element("connection", wscNamespace);

    copyAttributeIfPresent(object, connection, "service");
    object.removeAttribute("service");

    copyAttributeIfPresent(object, connection, "port");
    object.removeAttribute("port");

    copyAttributeIfPresent(object, connection, "wsdlLocation");
    migrateExpression(connection.getAttribute("wsdlLocation"), expressionMigrator);
    object.removeAttribute("wsdlLocation");

    copyAttributeIfPresent(object, connection, "serviceAddress", "address");
    migrateExpression(connection.getAttribute("address"), expressionMigrator);
    object.removeAttribute("serviceAddress");

    // TODO useConnectorToRetrieveWsdl?
    if (object.getAttribute("useConnectorToRetrieveWsdl") != null) {
      report.report(WARN, object, object,
                    "A connector will be used for retrieving the wsdl only if a connector is referenced from this config.");
      object.removeAttribute("useConnectorToRetrieveWsdl");
    }

    if (object.getAttribute("connectorConfig") != null) {
      connection.addContent(new Element("custom-transport-configuration", wscNamespace)
          .addContent(new Element("http-transport-configuration", wscNamespace)
              .setAttribute("requesterConfig", object.getAttributeValue("connectorConfig"))));
      object.removeAttribute("connectorConfig");
    } else if (object.getAttribute("connector-ref") != null) {
      String transportConnectorName = object.getAttributeValue("connector-ref");

      final Element requestConfig = new Element("request-config", httpNamespace).setAttribute("name", transportConnectorName);
      final Element requestConnection = new Element("request-connection", httpNamespace);

      requestConfig.addContent(requestConnection);
      object.getDocument().getRootElement().addContent(0, requestConfig);

      String address = connection.getAttributeValue("address");

      processAddress(connection, report).ifPresent(a -> {
        requestConnection.setAttribute("host", getExpressionMigrator().migrateExpression(a.getHost(), true, object));
        if (a.getPort() != null) {
          requestConnection.setAttribute("port", getExpressionMigrator().migrateExpression(a.getPort(), true, object));
        }
        requestConnection.setAttribute("protocol", "HTTP");
      });

      connection.setAttribute("address", address);

      Element connector = getApplicationModel().getNode("/mule:mule/http:connector[@name='" + transportConnectorName + "']");
      if (connector != null) {
        handleConnector(connector, requestConnection, report, wscNamespace, getApplicationModel());
      } else {
        connector = getApplicationModel().getNode("/mule:mule/https:connector[@name='" + transportConnectorName + "']");

        if (connector != null) {
          handleConnector(connector, requestConnection, report, wscNamespace, getApplicationModel());
          migrate(requestConnection, of(connector), report, getApplicationModel());
        }
      }

      connection.addContent(new Element("custom-transport-configuration", wscNamespace)
          .addContent(new Element("http-transport-configuration", wscNamespace)
              .setAttribute("requesterConfig", transportConnectorName)));
      object.removeAttribute("connector-ref");
    } else {
      // If the protocol is not http, lookup the apropiate connector
      // only https/jms transports supported

      String address = connection.getAttributeValue("address");

      processAddress(connection, report).ifPresent(a -> {
        if ("https".equals(a.getProtocol())) {
          List<Element> connectors = getApplicationModel().getNodes("/mule:mule/https:connector");
          if (connectors.isEmpty()) {
            return;
          }
          Element connector = connectors.iterator().next();

          final Element requestConfig =
              new Element("request-config", httpNamespace).setAttribute("name", connector.getAttributeValue("name"));
          final Element requestConnection = new Element("request-connection", httpNamespace);

          requestConfig.addContent(requestConnection);
          object.getDocument().getRootElement().addContent(0, requestConfig);

          requestConnection.setAttribute("host", getExpressionMigrator().migrateExpression(a.getHost(), true, object));
          if (a.getPort() != null) {
            requestConnection.setAttribute("port", getExpressionMigrator().migrateExpression(a.getPort(), true, object));
          }
          requestConnection.setAttribute("protocol", "HTTPS");

          handleConnector(connector, requestConnection, report, wscNamespace, getApplicationModel());
          migrate(requestConnection, of(connector), report, getApplicationModel());

          connection.addContent(new Element("custom-transport-configuration", wscNamespace)
              .addContent(new Element("http-transport-configuration", wscNamespace)
                  .setAttribute("requesterConfig", connector.getAttributeValue("name"))));
          object.removeAttribute("connector-ref");
        } else if ("jms".equals(a.getProtocol())) {
          // TODO MMT-24
        } else {
          report.report(ERROR, object, object, "WebService consumer only supports HTTP or JMS transports");
        }
      });

      connection.setAttribute("address", address);
    }

    object.addContent(connection);

    Namespace ws3Namespace = Namespace.getNamespace("ws", "http://www.mulesoft.org/schema/mule/ws");
    if (object.getChild("security", ws3Namespace) != null) {
      Namespace tlsNamespace = Namespace.getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");
      Element security = object.getChild("security", ws3Namespace);

      security.setName("web-service-security");
      security.setNamespace(wscNamespace);
      security.detach();

      connection.addContent(security);

      if (security.getChild("wss-sign", ws3Namespace) != null) {
        Element sign = security.getChild("wss-sign", ws3Namespace);

        sign.setName("sign-security-strategy");
        sign.setNamespace(wscNamespace);
        String tlsContextName = sign.getAttributeValue("tlsContext-ref");
        sign.removeAttribute("tlsContext-ref");

        // TODO signatureKeyIdentifier?
        Element tlsContext = getApplicationModel().getNode("/mule:mule/tls:context[@name='" + tlsContextName + "']");
        Element keyStoreConfig = new Element("key-store-configuration", wscNamespace);
        Element keyStore = tlsContext.getChild("key-store", tlsNamespace);

        copyAttributeIfPresent(keyStore, keyStoreConfig, "path", "keyStorePath");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "keyPassword");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "password");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "alias");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "type");
        sign.addContent(keyStoreConfig);

        if (getApplicationModel().getNodes("//*[@tlsContext-ref='" + tlsContextName + "']").isEmpty()) {
          tlsContext.detach();
        }

        sign.detach();
        security.addContent(sign);
      }

      if (security.getChild("wss-verify-signature", ws3Namespace) != null) {
        Element verifySignature = security.getChild("wss-verify-signature", ws3Namespace);

        verifySignature.setName("verify-signature-security-strategy");
        verifySignature.setNamespace(wscNamespace);
        String tlsContextName = verifySignature.getAttributeValue("tlsContext-ref");
        verifySignature.removeAttribute("tlsContext-ref");

        Element tlsContext = getApplicationModel().getNode("/mule:mule/tls:context[@name='" + tlsContextName + "']");
        Element keyStoreConfig = new Element("trust-store-configuration", wscNamespace);
        Element trustStore = tlsContext.getChild("trust-store", tlsNamespace);

        copyAttributeIfPresent(trustStore, keyStoreConfig, "path", "trustStorePath");
        copyAttributeIfPresent(trustStore, keyStoreConfig, "password");
        copyAttributeIfPresent(trustStore, keyStoreConfig, "alias");
        copyAttributeIfPresent(trustStore, keyStoreConfig, "type");
        verifySignature.addContent(keyStoreConfig);

        if (getApplicationModel().getNodes("//*[@tlsContext-ref='" + tlsContextName + "']").isEmpty()) {
          tlsContext.detach();
        }

        verifySignature.detach();
        security.addContent(verifySignature);
      }

      if (security.getChild("wss-username-token", ws3Namespace) != null) {
        Element userNameToken = security.getChild("wss-username-token", ws3Namespace);

        userNameToken.setName("username-token-security-strategy");
        userNameToken.setNamespace(wscNamespace);

        userNameToken.detach();
        security.addContent(userNameToken);
      }

      if (security.getChild("wss-timestamp", ws3Namespace) != null) {
        Element timestamp = security.getChild("wss-timestamp", ws3Namespace);

        // TODO checkResponseTimestamp?
        timestamp.setName("timestamp-security-strategy");
        timestamp.setNamespace(wscNamespace);
        timestamp.getAttribute("expires").setName("timeToLive");

        timestamp.detach();
        security.addContent(timestamp);
      }

      if (security.getChild("wss-decrypt", ws3Namespace) != null) {
        Element decrypt = security.getChild("wss-decrypt", ws3Namespace);

        decrypt.setName("decrypt-security-strategy");
        decrypt.setNamespace(wscNamespace);
        String tlsContextName = decrypt.getAttributeValue("tlsContext-ref");
        decrypt.removeAttribute("tlsContext-ref");

        Element tlsContext = getApplicationModel().getNode("/mule:mule/tls:context[@name='" + tlsContextName + "']");
        Element keyStoreConfig = new Element("key-store-configuration", wscNamespace);
        Element keyStore = tlsContext.getChild("key-store", tlsNamespace);

        copyAttributeIfPresent(keyStore, keyStoreConfig, "path", "keyStorePath");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "keyPassword");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "password");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "alias");
        copyAttributeIfPresent(keyStore, keyStoreConfig, "type");
        decrypt.addContent(keyStoreConfig);

        if (decrypt.getAttribute("alias") != null) {
          keyStoreConfig.setAttribute("alias", decrypt.getAttributeValue("alias"));
          decrypt.removeAttribute("alias");
        }

        if (getApplicationModel().getNodes("//*[@tlsContext-ref='" + tlsContextName + "']").isEmpty()) {
          tlsContext.detach();
        }

        decrypt.detach();
        security.addContent(decrypt);
      }

      if (security.getChild("wss-encrypt", ws3Namespace) != null) {
        Element encrypt = security.getChild("wss-encrypt", ws3Namespace);

        encrypt.setName("encrypt-security-strategy");
        encrypt.setNamespace(wscNamespace);
        String tlsContextName = encrypt.getAttributeValue("tlsContext-ref");
        encrypt.removeAttribute("tlsContext-ref");

        Element tlsContext = getApplicationModel().getNode("/mule:mule/tls:context[@name='" + tlsContextName + "']");
        Element keyStoreConfig = new Element("key-store-configuration", wscNamespace);
        Element trustStore = tlsContext.getChild("trust-store", tlsNamespace);

        copyAttributeIfPresent(trustStore, keyStoreConfig, "path", "keyStorePath");
        copyAttributeIfPresent(trustStore, keyStoreConfig, "password");
        copyAttributeIfPresent(trustStore, keyStoreConfig, "alias");
        copyAttributeIfPresent(trustStore, keyStoreConfig, "type");
        encrypt.addContent(keyStoreConfig);

        if (encrypt.getAttribute("alias") != null) {
          keyStoreConfig.setAttribute("alias", encrypt.getAttributeValue("alias"));
          encrypt.removeAttribute("alias");
        }

        if (getApplicationModel().getNodes("//*[@tlsContext-ref='" + tlsContextName + "']").isEmpty()) {
          tlsContext.detach();
        }

        encrypt.detach();
        security.addContent(encrypt);
      }
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
