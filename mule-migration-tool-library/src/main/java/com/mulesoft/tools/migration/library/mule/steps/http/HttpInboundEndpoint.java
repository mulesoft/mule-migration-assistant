/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.addAttributesToInboundProperties;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.compatibilityHeaders;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.handleReferencedResponseBuilder;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Migrates the inbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpInboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/mule:mule/mule:flow/http:inbound-endpoint[1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update HTTP transport inbound endpoint.";
  }

  public HttpInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);
    object.setName("listener");

    String flowName = object.getParentElement().getAttributeValue("name");
    String configName = (object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : flowName)).replaceAll("\\\\", "_")
        + "ListenerConfig";

    processAddress(object, report).ifPresent(address -> {
      extractListenerConfig(object, httpNamespace, configName, address.getHost(), address.getPort());

      if (address.getPath() != null) {
        if (address.getPath().endsWith("*")) {
          object.setAttribute("path", address.getPath());
        } else {
          object.setAttribute("path", address.getPath().endsWith("/") ? address.getPath() + "*" : address.getPath() + "/*");
        }
      }
    });
    if (object.getAttribute("host") != null && object.getAttribute("port") != null) {
      extractListenerConfig(object, httpNamespace, configName, object.getAttributeValue("host"),
                            object.getAttributeValue("port"));
      object.removeAttribute("host");
      object.removeAttribute("port");
    }

    if (object.getAttribute("connector-ref") != null) {
      Element connector = getConnector(object.getAttributeValue("connector-ref"));

      handleConnector(connector, report);

      object.removeAttribute("connector-ref");
    } else {
      getDefaultConnector().ifPresent(connector -> {
        handleConnector(connector, report);
      });
    }

    if (object.getAttribute("method") != null) {
      object.getAttribute("method").setName("allowedMethods");
    }

    if (object.getAttribute("path") == null) {
      object.setAttribute("path", "/*");
    } else {
      String path = object.getAttributeValue("path");
      if (!path.endsWith("*")) {
        object.setAttribute("path", path.endsWith("/") ? path + "*" : path + "/*");
      }
    }

    getApplicationModel()
        .getNodes("/mule:mule/mule:flow[@name='" + flowName + "']/http:response-builder")
        .forEach(rb -> {
          handleReferencedResponseBuilder(rb, getApplicationModel(), httpNamespace);
          Element response = getResponse(object, httpNamespace);
          handleResponseBuilder(object, response, rb, httpNamespace);

          copyAttributeIfPresent(rb, response, "statusCode");
          copyAttributeIfPresent(rb, response, "reasonPhrase");

          if (response.getAttribute("statusCode") == null) {
            response.setAttribute("statusCode", "#[vars.compatibility_outboundProperties['http.status'] default 200]");
            report.report(WARN, response, response, "Avoid using an outbound property to determine the status code.");
          }

          // if (rb.getAttribute("disablePropertiesAsHeaders") == null
          // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
          response.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));
          // }
        });

    getApplicationModel()
        .getNodes("/mule:mule/mule:flow[@name='" + flowName + "']/http:error-response-builder")
        .forEach(rb -> {
          handleReferencedResponseBuilder(rb, getApplicationModel(), httpNamespace);
          Element errorResponse = getErrorResponse(object, httpNamespace);
          handleResponseBuilder(object, errorResponse, rb, httpNamespace);
          copyAttributeIfPresent(rb, errorResponse, "statusCode");
          copyAttributeIfPresent(rb, errorResponse, "reasonPhrase");

          if (errorResponse.getAttribute("statusCode") == null) {
            errorResponse.setAttribute("statusCode", "#[vars.compatibility_outboundProperties['http.status']]");
            report.report(WARN, errorResponse, errorResponse, "Avoid using an outbound property to determine the status code.");
          }

          // if (rb.getAttribute("disablePropertiesAsHeaders") == null
          // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
          errorResponse.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));
          // }
        });

    if (object.getAttribute("contentType") != null) {
      Element response = getResponse(object, httpNamespace);
      response.addContent(new Element("header", httpNamespace)
          .setAttribute("headerName", "Content-Type")
          .setAttribute("value", object.getAttributeValue("contentType")));
      response.setAttribute("statusCode", "#[vars.compatibility_outboundProperties['http.status'] default 200]");
      report.report(WARN, response, response, "Avoid using an outbound property to determine the status code.");
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      response.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));
      object.removeAttribute("contentType");
    }

    Element response = object.getChild("response", httpNamespace);
    if (response == null) {
      response = getResponse(object, httpNamespace);
      response.setAttribute("statusCode", "#[vars.compatibility_outboundProperties['http.status'] default 200]");
      report.report(WARN, response, response, "Avoid using an outbound property to determine the status code.");
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      response.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));
      // }
    }
    Element errorResponse = object.getChild("error-response", httpNamespace);
    if (errorResponse == null) {
      errorResponse = getErrorResponse(object, httpNamespace);
      errorResponse.setAttribute("statusCode", "#[vars.compatibility_outboundProperties['http.status']]");
      report.report(WARN, errorResponse, errorResponse, "Avoid using an outbound property to determine the status code.");
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      errorResponse.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));
      // }
    }

    migrateInboundEndpointStructure(getApplicationModel(), object, report, true);
    addAttributesToInboundProperties(object, getApplicationModel(), report);

    // Replicates logic from org.mule.transport.http.HttpMuleMessageFactory#extractPayloadFromHttpRequest
    Element checkPayload = new Element("choice", CORE_NAMESPACE)
        .addContent(new Element("when", CORE_NAMESPACE)
            .setAttribute("expression", "#[(message.attributes.headers['Content-Length'] as Number default 0) == 0]")
            .addContent(new Element("set-payload", CORE_NAMESPACE).setAttribute("value", "#[message.attributes.requestUri]")));

    addElementAfter(checkPayload, object);
    report.report(WARN, checkPayload, checkPayload, "This replicates logic from the http transport. Remove if not needed.");

    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }
    if (object.getAttribute("ref") != null) {
      object.removeAttribute("ref");
    }
  }

  public static Element connectionHeaders(ApplicationModel appModel, Namespace httpNamespace) {

    // // Replicates logic from org.mule.transport.http.HttpMuleMessageFactory.rewriteConnectionAndKeepAliveHeaders(Map<String,
    // Object>)
    // expressionsPerProperty.put("Connection", "message.attributes.headers");
    // expressionsPerProperty.put("Keep-Alive", "message.attributes.headers");

    try {
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "HttpInboundConnectionAndKeepAliveHeaders.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the Connection and Keep-Alive inbound headers logic of the Mule 3.x HTTP Connector."
                  + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpInboundConnectionAndKeepAliveHeaders(version, headers: {}) = do {" + lineSeparator() +
                  // " var matcher_regex = /(?i)http\\..*|Connection|Transfer-Encoding/" + lineSeparator() +
                  // " ---" + lineSeparator() +
                  "    vars.compatibility_outboundProperties default {} filterObject" + lineSeparator() +
                  "        ((value,key) -> not ((key as String) matches matcher_regex))" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new Element("headers", httpNamespace)
        .setText("#[migration::HttpListenerHeaders::httpListenerResponseHeaders(vars)]");
  }

  private void extractListenerConfig(Element object, final Namespace httpNamespace, String configName, String host, String port) {
    List<Element> existingListener = getApplicationModel()
        .getNodes("/mule:mule/http:listener-config/http:listener-connection[@host = '" + host + "' and @port = '" + port
            + "']");
    if (!existingListener.isEmpty()) {
      existingListener.get(0);
      object.setAttribute("config-ref", existingListener.get(0).getParentElement().getAttributeValue("name"));
    } else {
      final Element listenerConfig = new Element("listener-config", httpNamespace).setAttribute("name", configName);
      final Element listenerConnection = new Element("listener-connection", httpNamespace);

      listenerConnection.setAttribute("host", host);
      listenerConnection.setAttribute("port", port);
      listenerConfig.addContent(listenerConnection);

      if (object.getAttribute("keepAlive") != null || object.getAttribute("keep-alive") != null) {
        copyAttributeIfPresent(object, listenerConnection, "keep-alive", "usePersistentConnections");
        copyAttributeIfPresent(object, listenerConnection, "keepAlive", "usePersistentConnections");
      } else {
        if (object.getAttribute("connector-ref") != null) {
          Element connector = getConnector(object.getAttributeValue("connector-ref"));
          if (connector.getAttribute("keepAlive") != null) {
            copyAttributeIfPresent(connector, listenerConnection, "keepAlive", "usePersistentConnections");
          }
        }
      }

      object.getDocument().getRootElement().addContent(0, listenerConfig);
      object.setAttribute("config-ref", configName);
    }
  }

  private void handleConnector(Element connector, MigrationReport report) {
    if (connector.getAttribute("serverSoTimeout") != null
        || connector.getAttribute("reuseAddress") != null) {
      // TODO MULE-14960
      report.report(ERROR, connector, connector, "The server socket properties have to be configured at the runtime level.");
      connector.removeAttribute("serverSoTimeout");
      connector.removeAttribute("reuseAddress");
    }
  }

  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/mule:mule/http:connector[@name = '" + connectorName + "']");
  }

  protected Optional<Element> getDefaultConnector() {
    List<Element> nodes = getApplicationModel().getNodes("/mule:mule/http:connector");
    return nodes.stream().findFirst();
  }

  private void handleResponseBuilder(Element listenerSource, Element listenerResponse, Element responseBuilder,
                                     Namespace httpNamespace) {
    if (responseBuilder.getChild("location", httpNamespace) != null) {
      Element location = responseBuilder.getChild("location", httpNamespace);
      listenerResponse.addContent(new Element("header", httpNamespace)
          .setAttribute("headerName", "Location")
          .setAttribute("value", location.getAttributeValue("value")));
      location.detach();
    }
    if (responseBuilder.getChild("expires", httpNamespace) != null) {
      Element expires = responseBuilder.getChild("expires", httpNamespace);
      listenerResponse.addContent(new Element("header", httpNamespace)
          .setAttribute("headerName", "Expires")
          .setAttribute("value", expires.getAttributeValue("value")));
      expires.detach();
    }
    if (responseBuilder.getChild("cache-control", httpNamespace) != null) {
      Element cacheControl = responseBuilder.getChild("cache-control", httpNamespace);

      if (cacheControl.getAttribute("directive") != null) {
        listenerResponse.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", "Cache-Control")
            .setAttribute("value", cacheControl.getAttributeValue("directive")));
      }
      if (cacheControl.getAttribute("noCache") != null && "true".equals(cacheControl.getAttributeValue("noCache"))) {
        listenerResponse.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", "Cache-Control")
            .setAttribute("value", "no-cache"));
      }
      if (cacheControl.getAttribute("noStore") != null && "true".equals(cacheControl.getAttributeValue("noStore"))) {
        listenerResponse.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", "Cache-Control")
            .setAttribute("value", "no-store"));
      }
      if (cacheControl.getAttribute("mustRevalidate") != null
          && "true".equals(cacheControl.getAttributeValue("mustRevalidate"))) {
        listenerResponse.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", "Cache-Control")
            .setAttribute("value", "must-revalidate"));
      }
      if (cacheControl.getAttribute("maxAge") != null) {
        listenerResponse.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", "Cache-Control")
            .setAttribute("value", "max-age=" + cacheControl.getAttributeValue("maxAge")));
      }

      cacheControl.detach();
    }

    new ArrayList<>(responseBuilder.getChildren("set-cookie", httpNamespace)).forEach(setCookie -> {
      StringBuilder cookieBuilder = new StringBuilder();

      cookieBuilder.append(setCookie.getAttributeValue("name") + "=" + setCookie.getAttributeValue("value") + "; ");
      if (setCookie.getAttribute("domain") != null) {
        cookieBuilder.append("Domain=" + setCookie.getAttributeValue("domain") + "; ");
      }
      if (setCookie.getAttribute("path") != null) {
        cookieBuilder.append("Path=" + setCookie.getAttributeValue("path") + "; ");
      }
      if (setCookie.getAttribute("expiryDate") != null) {
        cookieBuilder.append("Expires=" + setCookie.getAttributeValue("expiryDate") + "; ");
      }
      if (setCookie.getAttribute("secure") != null) {
        cookieBuilder.append("Secure; ");
      }
      if (setCookie.getAttribute("maxAge") != null) {
        cookieBuilder.append("Max-Age=" + setCookie.getAttributeValue("maxAge") + "; ");
      }

      listenerResponse.addContent(new Element("header", httpNamespace)
          .setAttribute("headerName", "Set-Cookie")
          .setAttribute("value", StringUtils.removeEnd(cookieBuilder.toString(), "; ")));

      setCookie.detach();
    });

    if (!responseBuilder.getChildren("header", httpNamespace).isEmpty()) {
      new ArrayList<>(responseBuilder.getChildren("header", httpNamespace)).forEach(h -> {
        h.detach();
        if (h.getAttribute("name") != null) {
          h.getAttribute("name").setName("headerName");
        }
        listenerResponse.addContent(h);
      });
    }

    if (!responseBuilder.getChildren().isEmpty()) {
      // i.e.: transformers are put at the end of the flow
      listenerSource.getParentElement().addContent(responseBuilder.cloneContent());
    }

    if (responseBuilder.getAttribute("status") != null) {
      listenerResponse.setAttribute("statusCode", responseBuilder.getAttributeValue("status"));
    }

    if (responseBuilder.getAttribute("contentType") != null) {
      listenerResponse.addContent(new Element("header", httpNamespace)
          .setAttribute("headerName", "Content-Type")
          .setAttribute("value", responseBuilder.getAttributeValue("contentType")));
    }

    responseBuilder.detach();
  }

  private Element getResponse(Element endpoint, Namespace httpNamespace) {
    Element response = endpoint.getChild("response", httpNamespace);
    if (response == null) {
      response = new Element("response", httpNamespace);

      endpoint.addContent(0, response);
    }
    return response;
  }

  private Element getErrorResponse(Element endpoint, Namespace httpNamespace) {
    Element response = endpoint.getChild("error-response", httpNamespace);
    if (response == null) {
      response = new Element("error-response", httpNamespace);
      endpoint.addContent(response);
    }
    return response;
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
