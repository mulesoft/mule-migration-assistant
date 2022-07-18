/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator.outboundVariable;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListener extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='listener']";
  public static final String NO_COMPATIBILITY_HEADERS_EXPRESSION =
      "vars filterObject ($$ startsWith 'outbound_') mapObject {($$ dw::core::Strings::substringAfter '_'): $}";

  @Override
  public String getDescription() {
    return "Update HTTP listener source.";
  }

  public HttpConnectorListener() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));
    object.setNamespace(HTTP_NAMESPACE);

    if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
      report.report("http.parseRequest", object, object);
    }
    object.removeAttribute("parseRequest");

    httpListenerLib(getApplicationModel());
    if (!getApplicationModel().noCompatibilityMode()) {
      migrateSourceStructureForCompatibility(getApplicationModel(), object, report);
      addAttributesToInboundProperties(object, getApplicationModel(), report);
    } else {
      report.report("noCompatibility.notFullyImplemented", object, object);
    }

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        executeChild(c, report, HTTP_NAMESPACE);
      }
    });

    if (object.getChild("response", HTTP_NAMESPACE) == null) {
      Element response = new Element("response", HTTP_NAMESPACE);
      object.addContent(0, response.addContent(addHeadersElement(HTTP_NAMESPACE)));
    }

    if (object.getChild("error-response", HTTP_NAMESPACE) == null) {
      Element errorResponse = new Element("error-response", HTTP_NAMESPACE);
      object.addContent(errorResponse.addContent(addHeadersElement(HTTP_NAMESPACE)));
    }

    addStatusCodeAttribute(object.getChild("response", HTTP_NAMESPACE), report,
                           "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]");
    addStatusCodeAttribute(object.getChild("error-response", HTTP_NAMESPACE), report,
                           "#[vars.statusCode default migration::HttpListener::httpListenerResponseErrorStatusCode(vars)]");
  }

  public static void addStatusCodeAttribute(Element element, MigrationReport report,
                                            String statusCodeDwScript) {
    if (element != null && element.getAttribute("statusCode") == null) {
      element.setAttribute("statusCode", statusCodeDwScript);
      report.report("http.statusCode", element, element);
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

  public void executeChild(Element object, MigrationReport report, Namespace httpNamespace)
      throws RuntimeException {
    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    switch (object.getName()) {
      case "response-builder": {
        handleReferencedResponseBuilder(object, getApplicationModel(), httpNamespace);
        object.setName("response");
        object.addContent(addHeadersElement(httpNamespace));
        break;
      }
      case "error-response-builder": {
        handleReferencedResponseBuilder(object, getApplicationModel(), httpNamespace);
        object.setName("error-response");
        object.addContent(addHeadersElement(httpNamespace));
        break;
      }
    }
  }

  public static Element addHeadersElement(Namespace httpNamespace) {
    return setText(new Element("headers", httpNamespace), "#[migration::HttpListener::httpListenerResponseHeaders(vars)]");
  }

  public static void httpListenerLib(ApplicationModel appModel) {
    try {
      String headersMap =
          appModel.noCompatibilityMode() ? NO_COMPATIBILITY_HEADERS_EXPRESSION : "vars.compatibility_outboundProperties";
      String varStatus =
          appModel.noCompatibilityMode() ? outboundVariable("http.status")
              : "vars.compatibility_outboundProperties['http.status']";

      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "HttpListener.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the response headers building logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpListenerResponseHeaders(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)http\\..*|Connection|Transfer-Encoding/" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    " + headersMap + " default {} filterObject" + lineSeparator() +
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
                  "    " + varStatus + " default 200" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Emulates the error status code logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpListenerResponseErrorStatusCode(vars: {}) = do {" + lineSeparator() +
                  "    " + varStatus + lineSeparator() +
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
