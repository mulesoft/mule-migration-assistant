/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.addAttributesToInboundProperties;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.compatibilityHeaders;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.handleReferencedResponseBuilder;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener.httpListenerLib;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Migrates the inbound endpoint of the HTTP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpInboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='inbound-endpoint'][1]";

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
    httpListenerLib(getApplicationModel());

    object.setNamespace(HTTP_NAMESPACE);
    object.setName("listener");

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    String flowName = getContainerElement(object).getAttributeValue("name");
    String configName = ((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : flowName)).replaceAll("\\\\", "_")
        + "ListenerConfig");

    processAddress(object, report).ifPresent(address -> {
      extractListenerConfig(getApplicationModel(), object, () -> getConnector(object.getAttributeValue("connector-ref")),
                            HTTP_NAMESPACE, configName, address.getHost(), address.getPort());

      if (address.getPath() != null) {
        if (address.getPath().endsWith("*")) {
          object.setAttribute("path", address.getPath());
        } else {
          object.setAttribute("path", address.getPath().endsWith("/") ? address.getPath() + "*" : address.getPath() + "/*");
        }
      }
    });
    if (object.getAttribute("host") != null && object.getAttribute("port") != null) {
      extractListenerConfig(getApplicationModel(), object, () -> getConnector(object.getAttributeValue("connector-ref")),
                            HTTP_NAMESPACE, configName, object.getAttributeValue("host"),
                            object.getAttributeValue("port"));
      object.removeAttribute("host");
      object.removeAttribute("port");
    }

    if (object.getAttribute("connector-ref") != null) {
      Element connector = getConnector(object.getAttributeValue("connector-ref"));

      handleConnector(connector, object, report);

      object.removeAttribute("connector-ref");
    } else {
      getDefaultConnector().ifPresent(connector -> {
        handleConnector(connector, object, report);
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
        .getNodes("/*/mule:flow[@name='" + flowName + "']/*[namespace-uri()='" + HTTP_NAMESPACE_URI
            + "' and local-name()='response-builder']")
        .forEach(rb -> {
          handleReferencedResponseBuilder(rb, getApplicationModel(), HTTP_NAMESPACE);
          Element response = getResponse(object, HTTP_NAMESPACE);
          handleResponseBuilder(object, response, rb, HTTP_NAMESPACE);

          copyAttributeIfPresent(rb, response, "statusCode");
          copyAttributeIfPresent(rb, response, "reasonPhrase");

          if (response.getAttribute("statusCode") == null) {
            response.setAttribute("statusCode", "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]");
            report.report("http.statusCode", response, response);
          }

          // if (rb.getAttribute("disablePropertiesAsHeaders") == null
          // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
          response.addContent(compatibilityHeaders(getApplicationModel(), HTTP_NAMESPACE));
          // }
        });

    getApplicationModel()
        .getNodes("/*/mule:flow[@name='" + flowName + "']/*[namespace-uri()='" + HTTP_NAMESPACE_URI
            + "' and local-name()='error-response-builder']")
        .forEach(rb -> {
          handleReferencedResponseBuilder(rb, getApplicationModel(), HTTP_NAMESPACE);
          Element errorResponse = getErrorResponse(object, HTTP_NAMESPACE);
          handleResponseBuilder(object, errorResponse, rb, HTTP_NAMESPACE);
          copyAttributeIfPresent(rb, errorResponse, "statusCode");
          copyAttributeIfPresent(rb, errorResponse, "reasonPhrase");

          if (errorResponse.getAttribute("statusCode") == null) {
            errorResponse
                .setAttribute("statusCode",
                              "#[vars.statusCode default migration::HttpListener::httpListenerResponseErrorStatusCode(vars)]");
            report.report("http.statusCode", errorResponse, errorResponse);
          }

          // if (rb.getAttribute("disablePropertiesAsHeaders") == null
          // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
          errorResponse.addContent(compatibilityHeaders(getApplicationModel(), HTTP_NAMESPACE));
          // }
        });

    if (object.getAttribute("contentType") != null) {
      Element response = getResponse(object, HTTP_NAMESPACE);
      response.addContent(new Element("header", HTTP_NAMESPACE)
          .setAttribute("headerName", "Content-Type")
          .setAttribute("value", object.getAttributeValue("contentType")));
      response.setAttribute("statusCode", "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]");
      report.report("http.statusCode", response, response);
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      response.addContent(compatibilityHeaders(getApplicationModel(), HTTP_NAMESPACE));
      object.removeAttribute("contentType");
    }

    Element response = object.getChild("response", HTTP_NAMESPACE);
    if (response == null) {
      response = getResponse(object, HTTP_NAMESPACE);
      response.setAttribute("statusCode", "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]");
      report.report("http.statusCode", response, response);
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      response.addContent(compatibilityHeaders(getApplicationModel(), HTTP_NAMESPACE));
      // }
    }
    Element errorResponse = object.getChild("error-response", HTTP_NAMESPACE);
    if (errorResponse == null) {
      errorResponse = getErrorResponse(object, HTTP_NAMESPACE);
      errorResponse.setAttribute("statusCode",
                                 "#[vars.statusCode default migration::HttpListener::httpListenerResponseErrorStatusCode(vars)]");
      report.report("http.statusCode", errorResponse, errorResponse,
                    "Avoid using an outbound property to determine the status code.");
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      errorResponse.addContent(compatibilityHeaders(getApplicationModel(), HTTP_NAMESPACE));
      // }
    }

    migrateInboundEndpointStructure(getApplicationModel(), object, report, true);
    addAttributesToInboundProperties(object, getApplicationModel(), report);

    // Replicates logic from org.mule.transport.http.HttpMuleMessageFactory#extractPayloadFromHttpRequest
    Element checkPayload = new Element("choice", CORE_NAMESPACE)
        .addContent(new Element("when", CORE_NAMESPACE)
            .setAttribute("expression",
                          "#[message.attributes.headers['Transfer-Encoding'] == null and (message.attributes.headers['Content-Length'] as Number default 0) == 0]")
            .addContent(new Element("set-payload", CORE_NAMESPACE).setAttribute("value", "#[message.attributes.requestUri]")));

    addElementAfter(checkPayload, object);
    report.report("http.checkPayload", checkPayload, checkPayload);

    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }
    if (object.getAttribute("ref") != null) {
      object.removeAttribute("ref");
    }
  }

  public static Element connectionHeaders(ApplicationModel appModel, Namespace httpNamespace) {
    try {
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "HttpInboundConnectionAndKeepAliveHeaders.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the Connection and Keep-Alive inbound headers logic of the Mule 3.x HTTP Connector."
                  + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpInboundConnectionAndKeepAliveHeaders(version, headers: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties default {} filterObject" + lineSeparator() +
                  "        ((value,key) -> not ((key as String) matches matcher_regex))" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return setText(new Element("headers", httpNamespace), "#[migration::HttpListener::httpListenerResponseHeaders(vars)]");
  }

  public static void extractListenerConfig(ApplicationModel appModel, Element object, Supplier<Element> connectorLookup,
                                           final Namespace httpNamespace,
                                           String configName, String host, String port) {
    Optional<Element> existingListener =
        appModel.getNodeOptional("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI
            + "' and local-name()='listener-config']/*[namespace-uri()='" + HTTP_NAMESPACE_URI
            + "' and local-name()='listener-connection' and @host = '" + host
            + "' and @port = '" + port + "']");

    if (existingListener.isPresent()) {
      object.setAttribute("config-ref", existingListener.get().getParentElement().getAttributeValue("name"));
    } else {
      final Element listenerConfig = new Element("listener-config", httpNamespace).setAttribute("name", configName);
      final Element listenerConnection = new Element("listener-connection", httpNamespace);

      listenerConnection.setAttribute("host", host);
      if (port != null) {
        listenerConnection.setAttribute("port", port);
      }
      listenerConfig.addContent(listenerConnection);

      if (object.getAttribute("keepAlive") != null || object.getAttribute("keep-alive") != null) {
        copyAttributeIfPresent(object, listenerConnection, "keep-alive", "usePersistentConnections");
        copyAttributeIfPresent(object, listenerConnection, "keepAlive", "usePersistentConnections");
      } else if (object.getAttribute("connector-ref") != null) {
        Element connector = connectorLookup.get();
        if (connector.getAttribute("keepAlive") != null) {
          copyAttributeIfPresent(connector, listenerConnection, "keepAlive", "usePersistentConnections");
        }
      }

      addTopLevelElement(listenerConfig, object.getDocument());
      object.setAttribute("config-ref", configName);
    }
  }

  private void handleConnector(Element connector, Element listener, MigrationReport report) {
    handleServiceOverrides(connector, report);

    if (connector.getAttribute("serverSoTimeout") != null
        || connector.getAttribute("reuseAddress") != null) {
      // TODO MULE-14960, MULE-15135
      report.report("http.socketProperties", connector, listener);
      connector.removeAttribute("serverSoTimeout");
      connector.removeAttribute("reuseAddress");
    }

    if (connector.getDocument().getRootElement().getName().equals("domain")) {
      report.report("transports.domainConnector", connector, connector);
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
      listenerResponse.setAttribute("statusCode", getExpressionMigrator()
          .migrateExpression(responseBuilder.getAttributeValue("status"), false, listenerResponse));
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
