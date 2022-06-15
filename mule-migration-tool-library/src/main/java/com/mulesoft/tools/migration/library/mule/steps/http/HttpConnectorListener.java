/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListener extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='listener']";
  public static final String NOCOMPATIBILITY_HEADERS_EXPRESSION =
      "#[vars filterObject ($$ startsWith 'outbound_') mapObject {($$ dw::core::Strings::substringAfter '_'): $}]";

  @Override
  public String getDescription() {
    return "Update HTTP listener source.";
  }

  public HttpConnectorListener() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    ApplicationGraph graph = getApplicationModel().getApplicationGraph();

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));

    object.setNamespace(HTTP_NAMESPACE);

    if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
      report.report("http.parseRequest", object, object);
    }
    object.removeAttribute("parseRequest");

    if (graph == null) {
      httpListenerLib(getApplicationModel());
      migrateSourceStructureForCompatibility(getApplicationModel(), object, report);
      addAttributesToInboundProperties(object, getApplicationModel(), report);
    } else {
      report.report("nocompatibility.notfullyimplemented", object, object);
    }

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        executeChild(c, report, HTTP_NAMESPACE, graph);
      }
    });

    if (object.getChild("response", HTTP_NAMESPACE) == null) {
      Element response = new Element("response", HTTP_NAMESPACE);
      object.addContent(0, response.addContent(addHeadersElement(HTTP_NAMESPACE, graph)));
    }

    if (object.getChild("error-response", HTTP_NAMESPACE) == null) {
      Element errorResponse = new Element("error-response", HTTP_NAMESPACE);
      object.addContent(errorResponse.addContent(addHeadersElement(HTTP_NAMESPACE, graph)));
    }

    addStatusCodeAttribute(object.getChild("response", HTTP_NAMESPACE), graph, report, "200",
                           "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]", getExpressionMigrator());
    addStatusCodeAttribute(object.getChild("error-response", HTTP_NAMESPACE), graph, report, "",
                           "#[vars.statusCode default migration::HttpListener::httpListenerResponseErrorStatusCode(vars)]",
                           getExpressionMigrator());
  }

  public static void addStatusCodeAttribute(Element element, ApplicationGraph graph, MigrationReport report,
                                            String defaultVal, String statusCodeDwScript, ExpressionMigrator migrator) {
    if (element != null && element.getAttribute("statusCode") == null) {
      if (graph != null) {
        addStatusCodeAttributeNoCompatibility(element, graph, defaultVal, migrator, report);
      } else {
        element.setAttribute("statusCode", statusCodeDwScript);
        report.report("http.statusCode", element, element);
      }
    }
  }

  public static void addStatusCodeAttributeNoCompatibility(Element element, ApplicationGraph graph, String defaultVal,
                                                           ExpressionMigrator migrator, MigrationReport report) {
    PropertiesSourceComponent propertiesSourceComponent =
        (PropertiesSourceComponent) graph.findFlowComponent(element.getParentElement());
    List<String> potentialTranslations = propertiesSourceComponent
        .getResponseComponent().getPropertiesMigrationContext().getOutboundTranslation("http.status", false);
    String statusCodeTranslation = defaultVal;
    if (!potentialTranslations.isEmpty()) {
      if (potentialTranslations.size() > 1) {
        report.report("nocompatibility.collidingProperties", element, element, element.getName());
      }
      statusCodeTranslation = potentialTranslations.get(0);
    }
    if (!StringUtils.isBlank(statusCodeTranslation)) {
      element.setAttribute("statusCode", migrator.wrap(statusCodeTranslation));
    }
  }

  public static Map<String, String> inboundToAttributesExpressions() {
    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.listener.path", "message.attributes.listenerPath");
    expressionsPerProperty.put("http.context.path",
                               "if (endsWith(message.attributes.listenerPath, '/*')) message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath");
    expressionsPerProperty.put("http.relative.path",
                               "message.attributes.requestPath[1 + sizeOf(if (endsWith(message.attributes.listenerPath, '/*')) message.attributes.listenerPath[0 to -3] default '/' else message.attributes.listenerPath) to -1]");
    expressionsPerProperty.put("http.version", "message.attributes.version");
    expressionsPerProperty.put("http.scheme", "message.attributes.scheme");
    expressionsPerProperty.put("http.method", "message.attributes.method");
    expressionsPerProperty.put("http.request.uri", "message.attributes.requestUri");
    expressionsPerProperty.put("http.query.string", "message.attributes.queryString");
    expressionsPerProperty.put("http.remote.address", "message.attributes.remoteAddress");
    expressionsPerProperty.put("http.client.cert", "message.attributes.clientCertificate");
    expressionsPerProperty.put("LOCAL_CERTIFICATES", "[message.attributes.clientCertificate]");
    expressionsPerProperty.put("PEER_CERTIFICATES", "[message.attributes.clientCertificate]");
    expressionsPerProperty.put("http.query.params", "message.attributes.queryParams");
    expressionsPerProperty.put("http.uri.params", "message.attributes.uriParams");
    expressionsPerProperty.put("http.request", "message.attributes.requestPath");
    expressionsPerProperty.put("http.request.path", "message.attributes.requestPath");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");
    return expressionsPerProperty;
  }

  public static void addAttributesToInboundProperties(Element object, ApplicationModel appModel, MigrationReport report) {
    try {
      addAttributesMapping(appModel, "org.mule.extension.http.api.HttpRequestAttributes", inboundToAttributesExpressions(),
                           "message.attributes.headers mapObject ((value, key, index) -> { (if(upper(key as String) startsWith 'X-MULE_') upper((key as String) [2 to -1]) else key) : value })",
                           "message.attributes.queryParams");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeChild(Element object, MigrationReport report, Namespace httpNamespace, ApplicationGraph graph)
      throws RuntimeException {
    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace, graph);
      }
    });

    switch (object.getName()) {
      case "response-builder": {
        handleReferencedResponseBuilder(object, getApplicationModel(), httpNamespace);
        object.setName("response");
        object.addContent(addHeadersElement(httpNamespace, graph));
        break;
      }
      case "error-response-builder": {
        handleReferencedResponseBuilder(object, getApplicationModel(), httpNamespace);
        object.setName("error-response");
        object.addContent(addHeadersElement(httpNamespace, graph));
        break;
      }
    }
  }

  public static Element addHeadersElement(Namespace httpNamespace, ApplicationGraph graph) {
    String headersText =
        graph != null ? NOCOMPATIBILITY_HEADERS_EXPRESSION : "#[migration::HttpListener::httpListenerResponseHeaders(vars)]";
    return setText(new Element("headers", httpNamespace), headersText);
  }

  public static void httpListenerLib(ApplicationModel appModel) {
    try {
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "HttpListener.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the response headers building logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpListenerResponseHeaders(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)http\\..*|Connection|Transfer-Encoding/" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars.compatibility_outboundProperties default {} filterObject" + lineSeparator() +
                  "        ((value,key) -> not ((key as String) matches matcher_regex))" + lineSeparator() +
                  "        mapObject ((value, key, index) -> {" + lineSeparator() +
                  "            (if (upper(key as String) startsWith 'MULE_') upper('X-' ++ key as String) else key) : value"
                  + lineSeparator() +
                  "        })" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Emulates the success status code logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpListenerResponseSuccessStatusCode(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties['http.status'] default 200" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Emulates the error status code logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpListenerResponseErrorStatusCode(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties['http.status']" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void handleReferencedResponseBuilder(Element object, ApplicationModel appModel, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      Element builder =
          appModel.getNode("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='response-builder' and @name='"
              + builderRef.getAttributeValue("ref") + "']");

      handleReferencedResponseBuilder(builder, appModel, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
    }
  }
}
