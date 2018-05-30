/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            : flowName))
        + "Config";

    final Element listenerConfig = new Element("listener-config", httpNamespace).setAttribute("name", configName);
    final Element listenerConnection = new Element("listener-connection", httpNamespace);

    listenerConfig.addContent(listenerConnection);
    object.getDocument().getRootElement().addContent(0, listenerConfig);

    processAddress(object, report).ifPresent(address -> {
      listenerConnection.setAttribute("host", address.getHost());
      listenerConnection.setAttribute("port", address.getPort());
      if (address.getPath() != null) {
        object.setAttribute("path", address.getPath());
      }
    });
    copyAttributeIfPresent(object, listenerConnection, "host");
    copyAttributeIfPresent(object, listenerConnection, "port");
    copyAttributeIfPresent(object, listenerConnection, "keep-alive", "usePersistentConnections");

    object.setAttribute("config-ref", configName);

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
      object.setAttribute("path", "/");
    }

    getApplicationModel()
        .getNodes("/mule:mule/mule:flow[@name='" + flowName + "']/http:response-builder")
        .forEach(rb -> {
          handleReferencedResponseBuilder(rb, httpNamespace);
          handleResponseBuilder(object, getResponse(object, httpNamespace), rb, httpNamespace);
        });

    getApplicationModel()
        .getNodes("/mule:mule/mule:flow[@name='" + flowName + "']/http:error-response-builder")
        .forEach(rb -> {
          handleReferencedResponseBuilder(rb, httpNamespace);
          handleResponseBuilder(object, getErrorResponse(object, httpNamespace), rb, httpNamespace);
        });

    if (object.getAttribute("contentType") != null) {
      getResponse(object, httpNamespace)
          .addContent(new Element("header", httpNamespace)
              .setAttribute("headerName", "Content-Type")
              .setAttribute("value", object.getAttributeValue("contentType")));
      object.removeAttribute("contentType");
    }

    addAttributesToInboundProperties(object, report);

    if (object.getAttribute("name") != null) {
      object.removeAttribute("name");
    }
    if (object.getAttribute("ref") != null) {
      object.removeAttribute("ref");
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

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, true);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.listener.path", "message.attributes.listenerPath");
    expressionsPerProperty.put("http.relative.path", "message.attributes.relativePath");
    expressionsPerProperty.put("http.version", "message.attributes.version");
    expressionsPerProperty.put("http.scheme", "message.attributes.scheme");
    expressionsPerProperty.put("http.method", "message.attributes.method");
    expressionsPerProperty.put("http.request.uri", "message.attributes.requestUri");
    expressionsPerProperty.put("http.query.string", "message.attributes.queryString");
    expressionsPerProperty.put("http.remote.address", "message.attributes.remoteAddress");
    expressionsPerProperty.put("http.client.cert", "message.attributes.clientCertificate");
    expressionsPerProperty.put("http.query.params", "message.attributes.queryParams");
    expressionsPerProperty.put("http.uri.params", "message.attributes.uriParams");
    expressionsPerProperty.put("http.request.path", "message.attributes.requestPath");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.http.api.HttpRequestAttributes", expressionsPerProperty);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleReferencedResponseBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      Element builder =
          getApplicationModel().getNode("/mule:mule/http:response-builder[@name='" + builderRef.getAttributeValue("ref") + "']");

      handleReferencedResponseBuilder(builder, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());
      builder.getParent().removeContent(builder);

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
    }
  }

  private Element getResponse(Element endpoint, Namespace httpNamespace) {
    Element response = endpoint.getChild("response", httpNamespace);
    if (response == null) {
      response = new Element("response", httpNamespace);
      endpoint.addContent(response);
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
