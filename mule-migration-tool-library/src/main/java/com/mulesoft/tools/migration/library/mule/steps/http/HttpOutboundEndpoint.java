/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static java.lang.System.lineSeparator;
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

  protected static final String SOCKETS_NAMESPACE = "http://www.mulesoft.org/schema/mule/sockets";

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
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    final Namespace socketsNamespace = Namespace.getNamespace("sockets", SOCKETS_NAMESPACE);
    object.setNamespace(httpNamespace);
    object.setName("request");

    String flowName = object.getParentElement().getAttributeValue("name");
    String configName = (object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : flowName))
        + "Config";

    final Element requestConfig = new Element("request-config", httpNamespace).setAttribute("name", configName);
    final Element requestConnection = new Element("request-connection", httpNamespace);

    requestConfig.addContent(requestConnection);
    object.getDocument().getRootElement().addContent(0, requestConfig);

    processAddress(object, report).ifPresent(address -> {
      requestConnection.setAttribute("host", getExpressionMigrator().migrateExpression(address.getHost(), true, object));
      requestConnection.setAttribute("port", getExpressionMigrator().migrateExpression(address.getPort(), true, object));
      if (address.getPath() != null) {
        object.setAttribute("path", getExpressionMigrator().migrateExpression(address.getPath(), true, object));
      }
    });
    copyAttributeIfPresent(object, requestConnection, "host");
    migrateExpression(requestConnection.getAttribute("host"), expressionMigrator);
    copyAttributeIfPresent(object, requestConnection, "port");
    migrateExpression(requestConnection.getAttribute("port"), expressionMigrator);
    copyAttributeIfPresent(object, requestConnection, "keep-alive", "usePersistentConnections");

    if (object.getAttribute("path") == null) {
      object.setAttribute("path", "/");
    }

    object.setAttribute("config-ref", configName);

    if (object.getAttribute("connector-ref") != null) {
      Element connector = getConnector(object.getAttributeValue("connector-ref"));

      handleConnector(connector, requestConnection, report, httpNamespace, socketsNamespace);

      object.removeAttribute("connector-ref");
    } else {
      getDefaultConnector().ifPresent(connector -> {
        handleConnector(connector, requestConnection, report, httpNamespace, socketsNamespace);
      });
    }

    if (object.getAttribute("keepAlive") != null) {
      getSocketProperties(requestConnection, httpNamespace, socketsNamespace)
          .setAttribute("keepAlive", object.getAttributeValue("keepAlive"));
      object.removeAttribute("keepAlive");
    }

    if (object.getAttribute("contentType") != null) {
      object.addContent(new Element("header", httpNamespace)
          .setAttribute("headerName", "Content-Type")
          .setAttribute("value", object.getAttributeValue("contentType")));
      object.removeAttribute("contentType");
    }

    if (object.getAttribute("exceptionOnMessageError") != null
        && "false".equals(object.getAttributeValue("exceptionOnMessageError"))) {

      object.addContent(new Element("response-validator", httpNamespace)
          .addContent(new Element("success-status-code-validator", httpNamespace).setAttribute("values", "0..599")));

      object.removeAttribute("exceptionOnMessageError");
    }
    addAttributesToInboundProperties(object, report);

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
                               Namespace httpNamespace, Namespace socketsNamespace) {
    if (connector.getAttribute("connectionTimeout") != null) {
      getSocketProperties(reqConnection, httpNamespace, socketsNamespace)
          .setAttribute("connectionTimeout", connector.getAttributeValue("connectionTimeout"));
    }
    if (connector.getAttribute("clientSoTimeout") != null) {
      getSocketProperties(reqConnection, httpNamespace, socketsNamespace)
          .setAttribute("clientTimeout", connector.getAttributeValue("clientSoTimeout"));
    }
  }

  private Element getSocketProperties(Element reqConnection, Namespace httpNamespace, Namespace socketsNamespace) {
    Element socketProps = new Element("tcp-client-socket-properties", socketsNamespace);
    reqConnection.addContent(new Element("client-socket-properties", httpNamespace)
        .addContent(socketProps));
    return socketProps;
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateOutboundEndpointStructure(getApplicationModel(), object, report, true);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.status", "message.attributes.statusCode");
    expressionsPerProperty.put("http.reason", "message.attributes.reasonPhrase");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.http.api.HttpResponseAttributes", expressionsPerProperty);
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
    try {
      library(getMigrationScriptFolder(getApplicationModel().getProjectBasePath()), "HttpRequesterHeaders.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the request headers building logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpRequesterHeaders(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)http\\..*|Connection|Host|Transfer-Encoding/" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars.compatibility_outboundProperties filterObject" + lineSeparator() +
                  "        ((value,key) -> not ((key as String) matches matcher_regex))" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new Element("headers", httpNamespace)
        .setText("#[migration::HttpRequesterHeaders::httpRequesterHeaders(vars)]");
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
