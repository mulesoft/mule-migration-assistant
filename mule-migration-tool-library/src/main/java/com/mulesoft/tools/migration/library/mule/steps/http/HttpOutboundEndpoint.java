/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester.httpRequesterLib;
import static com.mulesoft.tools.migration.library.mule.steps.http.SocketsConfig.SOCKETS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.SocketsConfig.addSocketsModule;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Migrates the outbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpOutboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule//http:outbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update HTTP transport outbound endpoint.";
  }

  public HttpOutboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    httpRequesterLib(getApplicationModel());


    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);
    object.setName("request");

    String flowName = object.getParentElement().getAttributeValue("name");
    String configName = (object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : flowName)).replaceAll("\\\\", "_")
        + "RequestConfig";

    final Element requestConfig = new Element("request-config", httpNamespace).setAttribute("name", configName);
    final Element requestConnection = new Element("request-connection", httpNamespace);

    requestConfig.addContent(requestConnection);
    object.getDocument().getRootElement().addContent(0, requestConfig);

    processAddress(object, report).ifPresent(address -> {
      requestConnection.setAttribute("host", getExpressionMigrator().migrateExpression(address.getHost(), true, object));
      if (address.getPort() != null) {
        requestConnection.setAttribute("port", getExpressionMigrator().migrateExpression(address.getPort(), true, object));
      }
      if (address.getPath() != null) {
        object.setAttribute("path", getExpressionMigrator().migrateExpression(address.getPath(), true, object));
      }
    });
    copyAttributeIfPresent(object, requestConnection, "host");
    migrateExpression(requestConnection.getAttribute("host"), expressionMigrator);
    copyAttributeIfPresent(object, requestConnection, "port");
    migrateExpression(requestConnection.getAttribute("port"), expressionMigrator);

    if (object.getAttribute("keepAlive") != null || object.getAttribute("keep-alive") != null) {
      copyAttributeIfPresent(object, requestConnection, "keep-alive", "usePersistentConnections");
      copyAttributeIfPresent(object, requestConnection, "keepAlive", "usePersistentConnections");
    } else {
      if (object.getAttribute("connector-ref") != null) {
        Element connector = getConnector(object.getAttributeValue("connector-ref"));
        if (connector.getAttribute("keepAlive") != null) {
          copyAttributeIfPresent(connector, requestConnection, "keepAlive", "usePersistentConnections");
        }
      }
    }

    if (object.getAttribute("path") == null) {
      object.setAttribute("path", "/");
    }

    object.setAttribute("config-ref", configName);

    if (object.getAttribute("connector-ref") != null) {
      Element connector = getConnector(object.getAttributeValue("connector-ref"));

      handleConnector(connector, requestConnection, report, httpNamespace);

      object.removeAttribute("connector-ref");
    } else {
      getDefaultConnector().ifPresent(connector -> {
        handleConnector(connector, requestConnection, report, httpNamespace);
      });
    }

    if (object.getAttribute("method") == null) {
      // Logic from org.mule.transport.http.transformers.ObjectToHttpClientMethodRequest.detectHttpMethod(MuleMessage)
      object.setAttribute("method", "#[migration::HttpRequester::httpRequesterMethod(vars)]");
      report.report(WARN, object, object, "Avoid using an outbound property to determine the method.");
    }

    addAttributesToInboundProperties(object, report);

    if (object.getAttribute("contentType") != null) {
      object.getParentElement().addContent(object.getParentElement().indexOf(object), new Element("set-payload", CORE_NAMESPACE)
          .setAttribute("value", "#[payload]")
          .setAttribute("mimeType", object.getAttributeValue("contentType")));
      object.removeAttribute("contentType");
    }
    object.addContent(compatibilityHeaders(httpNamespace));

    if (object.getAttribute("exceptionOnMessageError") != null
        && "false".equals(object.getAttributeValue("exceptionOnMessageError"))) {

      object.addContent(new Element("response-validator", httpNamespace)
          .addContent(new Element("success-status-code-validator", httpNamespace).setAttribute("values", "0..599")));

      object.removeAttribute("exceptionOnMessageError");
    }
    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }
  }

  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/mule:mule/http:connector[@name = '" + connectorName + "']");
  }

  protected Optional<Element> getDefaultConnector() {
    List<Element> nodes = getApplicationModel().getNodes("/mule:mule/http:connector");
    return nodes.stream().findFirst();
  }

  private void handleConnector(Element connector, Element reqConnection, MigrationReport report,
                               Namespace httpNamespace) {
    if (connector.getAttribute("connectionTimeout") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("connectionTimeout", connector.getAttributeValue("connectionTimeout"));
    }
    if (connector.getAttribute("clientSoTimeout") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("clientTimeout", connector.getAttributeValue("clientSoTimeout"));
    }
    if (connector.getAttribute("sendTcpNoDelay") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("sendTcpNoDelay", connector.getAttributeValue("sendTcpNoDelay"));
    }

    if (connector.getAttribute("sendBufferSize") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("sendBufferSize", connector.getAttributeValue("sendBufferSize"));
    }
    if (connector.getAttribute("receiveBufferSize") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("receiveBufferSize", connector.getAttributeValue("receiveBufferSize"));
    }
    if (connector.getAttribute("socketSoLinger") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("linger", connector.getAttributeValue("socketSoLinger"));
    }
    if (connector.getAttribute("failOnUnresolvedHost") != null) {
      getSocketProperties(reqConnection, httpNamespace)
          .setAttribute("failOnUnresolvedHost", connector.getAttributeValue("failOnUnresolvedHost"));
    }

    if (connector.getAttribute("proxyHostname") != null) {
      getProxyConfig(reqConnection, httpNamespace)
          .setAttribute("host", connector.getAttributeValue("proxyHostname"));
    }
    if (connector.getAttribute("proxyPort") != null) {
      getProxyConfig(reqConnection, httpNamespace)
          .setAttribute("port", connector.getAttributeValue("proxyPort"));
    }
    if (connector.getAttribute("proxyUsername") != null) {
      getProxyConfig(reqConnection, httpNamespace)
          .setAttribute("username", connector.getAttributeValue("proxyUsername"));
    }
    if (connector.getAttribute("proxyPassword") != null) {
      getProxyConfig(reqConnection, httpNamespace)
          .setAttribute("password", connector.getAttributeValue("proxyPassword"));
    }

    if (connector.getAttribute("enableCookies") != null) {
      report.report(WARN, connector, reqConnection,
                    "Cookie support in Mule 4 is limited to resending any cookie received by the server before.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-http");
      copyAttributeIfPresent(connector, reqConnection.getParentElement(), "enableCookies");
    }
  }

  private Element getSocketProperties(Element reqConnection, Namespace httpNamespace) {
    addSocketsModule(getApplicationModel());

    Element clientSocket = reqConnection.getChild("client-socket-properties", httpNamespace);
    if (clientSocket != null) {
      Element tcpClientSocket = clientSocket.getChild("tcp-client-socket-properties", SOCKETS_NAMESPACE);
      if (tcpClientSocket != null) {
        return tcpClientSocket;
      } else {
        tcpClientSocket = new Element("tcp-client-socket-properties", SOCKETS_NAMESPACE);
        clientSocket.addContent(tcpClientSocket);
        return tcpClientSocket;
      }
    } else {
      Element socketProps = new Element("tcp-client-socket-properties", SOCKETS_NAMESPACE);
      reqConnection.addContent(new Element("client-socket-properties", httpNamespace)
          .addContent(socketProps));
      return socketProps;
    }
  }

  private Element getProxyConfig(Element reqConnection, Namespace httpNamespace) {
    Element clientSocket = reqConnection.getChild("proxy-config", httpNamespace);
    if (clientSocket != null) {
      Element tcpClientSocket = clientSocket.getChild("proxy", httpNamespace);
      if (tcpClientSocket != null) {
        return tcpClientSocket;
      } else {
        tcpClientSocket = new Element("proxy", httpNamespace);
        clientSocket.addContent(tcpClientSocket);
        return tcpClientSocket;
      }
    } else {
      Element socketProps = new Element("proxy", httpNamespace);
      reqConnection.addContent(new Element("proxy-config", httpNamespace)
          .addContent(socketProps));
      return socketProps;
    }

  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.status", "message.attributes.statusCode");
    expressionsPerProperty.put("http.reason", "message.attributes.reasonPhrase");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.http.api.HttpResponseAttributes", expressionsPerProperty,
                           "message.attributes.headers");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeChild(Element object, MigrationReport report, Namespace httpNamespace) throws RuntimeException {
    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if ("request-builder".equals(object.getName())) {
      handleReferencedRequestBuilder(object, httpNamespace);
      object.addContent(compatibilityHeaders(httpNamespace));
    }
  }

  private Element compatibilityHeaders(Namespace httpNamespace) {
    return new Element("headers", httpNamespace)
        .setText("#[migration::HttpRequester::httpRequesterTransportHeaders(vars)]");
  }

  private void handleReferencedRequestBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      Element builder =
          getApplicationModel().getNode("/mule:mule/http:request-builder[@name='" + builderRef.getAttributeValue("ref") + "']");

      handleReferencedRequestBuilder(builder, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());
      builder.getParent().removeContent(builder);

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
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
