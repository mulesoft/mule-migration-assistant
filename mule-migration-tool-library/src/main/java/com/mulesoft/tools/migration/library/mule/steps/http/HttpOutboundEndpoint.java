/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester.addAttributesToInboundProperties;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester.httpRequesterLib;
import static com.mulesoft.tools.migration.library.mule.steps.http.SocketsConfig.SOCKETS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.SocketsConfig.addSocketsModule;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

/**
 * Migrates the outbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpOutboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='outbound-endpoint']";

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

    object.setNamespace(HTTP_NAMESPACE);
    object.setName("request");

    Element flow = getContainerElement(object);
    String flowName = flow.getAttributeValue("name") != null ? flow.getAttributeValue("name")
        : flow.getParentElement().getName() + StringUtils.capitalize(flow.getName());
    String configName = (object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : flowName)).replaceAll("\\\\", "_")
        + "RequestConfig";

    Optional<Element> nodeOptional = getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='request-config' and @name='"
            + configName + "']/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='request-connection']");

    if (nodeOptional.isPresent()) {
      // If there are multiple outbound endpoints in a flow, generate a config for each one, with an index appended
      configName = configName + object.getParentElement().indexOf(object);
    }

    final Element requestConfig = new Element("request-config", HTTP_NAMESPACE).setAttribute("name", configName);
    final Element requestConnection = new Element("request-connection", HTTP_NAMESPACE);

    requestConfig.addContent(requestConnection);
    addTopLevelElement(requestConfig, nodeOptional.map(n -> n.getDocument()).orElse(object.getDocument()));

    copyAttributeIfPresent(object, requestConnection, "host");
    migrateExpression(requestConnection.getAttribute("host"), expressionMigrator);
    copyAttributeIfPresent(object, requestConnection, "port");
    migrateExpression(requestConnection.getAttribute("port"), expressionMigrator);

    processAddress(object, report).ifPresent(address -> {
      requestConnection.setAttribute("host", getExpressionMigrator().migrateExpression(address.getHost(), true, object));
      if (address.getPort() != null) {
        requestConnection.setAttribute("port", getExpressionMigrator().migrateExpression(address.getPort(), true, object));
      }
      if (address.getPath() != null) {
        object.setAttribute("path", getExpressionMigrator().migrateExpression(address.getPath(), true, object));
      }
    });
    if (object.getAttribute("keepAlive") != null || object.getAttribute("keep-alive") != null) {
      copyAttributeIfPresent(object, requestConnection, "keep-alive", "usePersistentConnections");
      copyAttributeIfPresent(object, requestConnection, "keepAlive", "usePersistentConnections");
    }

    if (object.getAttribute("path") == null) {
      object.setAttribute("path", "/");
    }

    object.setAttribute("config-ref", configName);

    if (object.getAttribute("connector-ref") != null) {
      Element connector = getConnector(object.getAttributeValue("connector-ref"));

      handleConnector(connector, requestConnection, report, HTTP_NAMESPACE, getApplicationModel());

      object.removeAttribute("connector-ref");
    } else {
      getDefaultConnector().ifPresent(connector -> {
        handleConnector(connector, requestConnection, report, HTTP_NAMESPACE, getApplicationModel());
      });
    }

    migrateExpression(object.getAttribute("method"), expressionMigrator);

    if (object.getAttribute("method") == null) {
      // Logic from org.mule.transport.http.transformers.ObjectToHttpClientMethodRequest.detectHttpMethod(MuleMessage)
      object.setAttribute("method", "#[migration::HttpRequester::httpRequesterMethod(vars)]");
      object.setAttribute("sendBodyMode", getExpressionMigrator()
          .wrap("if (migration::HttpRequester::httpRequesterMethod(vars) == 'DELETE') 'NEVER' else 'AUTO'"));
      report.report("http.method", object, object);
      report.report("http.sendBodyMode", object, object);
    } else {
      if ("DELETE".equals(object.getAttributeValue("method"))) {
        object.setAttribute("sendBodyMode", "NEVER");
        report.report("http.sendBodyMode", object, object);
      } else if (getExpressionMigrator().isWrapped(object.getAttributeValue("method"))) {
        object.setAttribute("sendBodyMode", getExpressionMigrator().wrap("if ("
            + getExpressionMigrator().unwrap(object.getAttributeValue("method")) + " == 'DELETE') 'NEVER' else 'AUTO'"));
        report.report("http.sendBodyMode", object, object);
      }
    }

    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true);
    addAttributesToInboundProperties(object, getApplicationModel(), report);

    if (object.getAttribute("contentType") != null) {
      String contentType = object.getAttributeValue("contentType").toLowerCase();
      if (contentType.startsWith("application/dw")
          || contentType.startsWith("application/java")
          || contentType.startsWith("application/json")
          || contentType.startsWith("application/xml")
          || contentType.startsWith("application/csv")
          || contentType.startsWith("application/octet-stream")
          || contentType.startsWith("text/plain")
          || contentType.startsWith("application/x-www-form-urlencoded")
          || contentType.startsWith("multipart/form-data")
          || contentType.startsWith("text/x-java-properties")
          || contentType.startsWith("application/yaml")) {
        object.getParentElement().addContent(object.getParentElement().indexOf(object), new Element("set-payload", CORE_NAMESPACE)
            .setAttribute("value", "#[output " + object.getAttributeValue("contentType") + " --- payload]"));
        object.removeAttribute("contentType");
      } else {
        object.addContent(new Element("header", HTTP_NAMESPACE)
            .setAttribute("headerName", "Content-Type")
            .setAttribute("value", object.getAttributeValue("contentType")));
      }
      object.removeAttribute("contentType");
    }
    object.addContent(compatibilityHeaders(HTTP_NAMESPACE));

    if (object.getAttribute("exceptionOnMessageError") != null
        && "false".equals(object.getAttributeValue("exceptionOnMessageError"))) {

      object.addContent(new Element("response-validator", HTTP_NAMESPACE)
          .addContent(new Element("success-status-code-validator", HTTP_NAMESPACE).setAttribute("values", "0..599")));

      object.removeAttribute("exceptionOnMessageError");
    }
    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }
  }

  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI
        + "' and local-name()='connector' and @name = '" + connectorName + "']");
  }

  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='connector']");
  }

  public static void handleConnector(Element connector, Element reqConnection, MigrationReport report,
                                     Namespace httpNamespace, ApplicationModel appModel) {
    handleServiceOverrides(connector, report);

    if (connector.getAttribute("keepAlive") != null && reqConnection.getAttribute("usePersistentConnections") == null) {
      copyAttributeIfPresent(connector, reqConnection, "keepAlive", "usePersistentConnections");
    }

    if (connector.getAttribute("connectionTimeout") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
          .setAttribute("connectionTimeout", connector.getAttributeValue("connectionTimeout"));
    }
    if (connector.getAttribute("clientSoTimeout") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
          .setAttribute("clientTimeout", connector.getAttributeValue("clientSoTimeout"));
    }
    if (connector.getAttribute("sendTcpNoDelay") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
          .setAttribute("sendTcpNoDelay", connector.getAttributeValue("sendTcpNoDelay"));
    }

    if (connector.getAttribute("sendBufferSize") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
          .setAttribute("sendBufferSize", connector.getAttributeValue("sendBufferSize"));
    }
    if (connector.getAttribute("receiveBufferSize") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
          .setAttribute("receiveBufferSize", connector.getAttributeValue("receiveBufferSize"));
    }
    if (connector.getAttribute("socketSoLinger") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
          .setAttribute("linger", connector.getAttributeValue("socketSoLinger"));
    }
    if (connector.getAttribute("failOnUnresolvedHost") != null) {
      getSocketProperties(reqConnection, httpNamespace, appModel)
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
      report.report("http.cookies", connector, reqConnection);
      copyAttributeIfPresent(connector, reqConnection.getParentElement(), "enableCookies");
    }

    if (connector.getDocument().getRootElement().getName().equals("domain")) {
      report.report("transports.domainConnector", connector, connector);
    }
  }

  private static Element getSocketProperties(Element reqConnection, Namespace httpNamespace, ApplicationModel appModel) {
    addSocketsModule(appModel);

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

  private static Element getProxyConfig(Element reqConnection, Namespace httpNamespace) {
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

  public void executeChild(Element object, MigrationReport report, Namespace httpNamespace) throws RuntimeException {
    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if ("request-builder".equals(object.getName())) {
      handleReferencedRequestBuilder(object, httpNamespace);
      object.addContent(compatibilityHeaders(httpNamespace));
    }
  }

  private Element compatibilityHeaders(Namespace httpNamespace) {
    return setText(new Element("headers", httpNamespace), "#[migration::HttpRequester::httpRequesterTransportHeaders(vars)]");
  }

  private void handleReferencedRequestBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      Element builder =
          getApplicationModel().getNode("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI
              + "' and local-name()='request-builder' and @name='" + builderRef.getAttributeValue("ref") + "']");

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
