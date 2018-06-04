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
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateSourceStructure;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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

  public static final String XPATH_SELECTOR = "/mule:mule/mule:flow/http:listener";

  @Override
  public String getDescription() {
    return "Update HTTP listener source.";
  }

  public HttpConnectorListener() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    httpListenerLib(getApplicationModel());

    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
      report.report(WARN, object, object,
                    "'parseRequest' is not needed in Mule 4, since the InputStream of the multipart payload is provided at it is read.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-connectors-http#http-mime-types",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/dataweave-formats#format_form_data");
    }
    object.removeAttribute("parseRequest");

    migrateSourceStructure(getApplicationModel(), object, report);
    addAttributesToInboundProperties(object, getApplicationModel(), report);

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if (object.getChild("response", httpNamespace) == null) {
      Element response = new Element("response", httpNamespace);
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      object.addContent(0, response.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace)));
      // }
    }
    Element response = object.getChild("response", httpNamespace);
    if (response.getAttribute("statusCode") == null) {
      response.setAttribute("statusCode", "#[migration::HttpListener::httpListenerResponseSuccessStatusCode(vars)]");
      report.report(WARN, response, response, "Avoid using an outbound property to determine the status code.");
    }
    if (object.getChild("error-response", httpNamespace) == null) {
      Element errorResponse = new Element("error-response", httpNamespace);
      // if (rb.getAttribute("disablePropertiesAsHeaders") == null
      // || "false".equals(rb.getAttributeValue("disablePropertiesAsHeaders"))) {
      object.addContent(errorResponse.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace)));
      // }
    }
    Element errorResponse = object.getChild("error-response", httpNamespace);
    if (errorResponse.getAttribute("statusCode") == null) {
      errorResponse.setAttribute("statusCode", "#[migration::HttpListener::httpListenerResponseErrorStatusCode(vars)]");
      report.report(WARN, errorResponse, errorResponse, "Avoid using an outbound property to determine the status code.");
    }
  }

  public static void addAttributesToInboundProperties(Element object, ApplicationModel appModel, MigrationReport report) {
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
    expressionsPerProperty.put("http.query.params", "message.attributes.queryParams");
    expressionsPerProperty.put("http.uri.params", "message.attributes.uriParams");
    expressionsPerProperty.put("http.request", "message.attributes.requestPath");
    expressionsPerProperty.put("http.request.path", "message.attributes.requestPath");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(appModel, "org.mule.extension.http.api.HttpRequestAttributes", expressionsPerProperty,
                           "message.attributes.headers", "message.attributes.queryParams");
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

    if ("response-builder".equals(object.getName())) {
      handleReferencedResponseBuilder(object, getApplicationModel(), httpNamespace);
      object.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));

      object.setName("response");
    }
    if ("error-response-builder".equals(object.getName())) {
      handleReferencedResponseBuilder(object, getApplicationModel(), httpNamespace);
      object.addContent(compatibilityHeaders(getApplicationModel(), httpNamespace));

      object.setName("error-response");
    }
  }

  public static Element compatibilityHeaders(ApplicationModel appModel, Namespace httpNamespace) {
    return new Element("headers", httpNamespace)
        .setText("#[migration::HttpListener::httpListenerResponseHeaders(vars)]");
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
          appModel.getNode("/mule:mule/http:response-builder[@name='" + builderRef.getAttributeValue("ref") + "']");

      handleReferencedResponseBuilder(builder, appModel, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
    }
  }
}
