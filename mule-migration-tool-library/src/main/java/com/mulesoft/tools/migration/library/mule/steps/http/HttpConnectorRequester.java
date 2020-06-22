/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateExpression;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.library.tools.mel.MelCompatibilityResolver;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates the requester operation of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorRequester extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='request']";

  @Override
  public String getDescription() {
    return "Update HTTP requester operation.";
  }

  public HttpConnectorRequester() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    httpRequesterLib(getApplicationModel());

    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE_URI);
    object.setNamespace(httpNamespace);

    if (object.getAttribute("port") != null) {
      report.report("http.port", object, object, object.getAttributeValue("port"));
      object.removeAttribute("port");
    }
    if (object.getAttribute("host") != null) {
      report.report("http.host", object, object, object.getAttributeValue("host"));
      object.removeAttribute("host");
    }
    if (object.getAttribute("parseResponse") != null) {
      report.report("http.parseResponse", object, object, object.getAttributeValue("parseResponse"));
      object.removeAttribute("parseResponse");
    }

    migrateExpression(object.getAttribute("path"), getExpressionMigrator());
    migrateExpression(object.getAttribute("method"), getExpressionMigrator());
    migrateExpression(object.getAttribute("followRedirects"), getExpressionMigrator());

    migrateOperationStructure(getApplicationModel(), object, report, true, getExpressionMigrator(),
                              new MelCompatibilityResolver());
    addAttributesToInboundProperties(object, getApplicationModel(), report);

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if (object.getChild("request-builder", httpNamespace) == null) {
      object.addContent(new Element("request-builder", httpNamespace).addContent(compatibilityHeaders(httpNamespace)));
    }

    if (object.getAttribute("source") != null) {
      if (!"#[payload]".equals(object.getAttributeValue("source"))) {
        object.addContent(setText(new Element("body", httpNamespace), getExpressionMigrator()
            .wrap(getExpressionMigrator().migrateExpression(object.getAttributeValue("source"), true, object))));
      }
      object.removeAttribute("source");
    }

    Element requestBuilder = object.getChild("request-builder", httpNamespace);
    if (requestBuilder != null) {
      object.removeContent(requestBuilder);

      Builder<Content> listBuilder = ImmutableList.<Content>builder();

      listBuilder.addAll(requestBuilder.getContent().stream()
          .filter(c -> c instanceof Element
              && ("header".equals(((Element) c).getName()) || "headers".equals(((Element) c).getName())))
          .collect(toList()));
      listBuilder.addAll(requestBuilder.getContent().stream()
          .filter(c -> c instanceof Element
              && ("uri-param".equals(((Element) c).getName()) || "uri-params".equals(((Element) c).getName())))
          .collect(toList()));
      listBuilder.addAll(requestBuilder.getContent().stream()
          .filter(c -> c instanceof Element
              && ("query-param".equals(((Element) c).getName()) || "query-params".equals(((Element) c).getName())))
          .collect(toList()));

      List<Content> builderContent = listBuilder.build();

      requestBuilder.setContent(emptyList());
      object.addContent(builderContent);
    }

    Element responseValidator = null;
    Element successValidator = object.getChild("success-status-code-validator", httpNamespace);
    if (successValidator != null) {
      object.removeContent(successValidator);

      responseValidator = new Element("response-validator", httpNamespace);
      object.addContent(responseValidator);

      responseValidator.addContent(successValidator);
    }
    Element failureValidator = object.getChild("failure-status-code-validator", httpNamespace);
    if (failureValidator != null) {
      object.removeContent(failureValidator);

      if (responseValidator == null) {
        responseValidator = new Element("response-validator", httpNamespace);
        object.addContent(responseValidator);
      }

      responseValidator.addContent(failureValidator);
    }
  }

  public static void addAttributesToInboundProperties(Element object, ApplicationModel appModel, MigrationReport report) {
    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.status", "message.attributes.statusCode");
    expressionsPerProperty.put("http.reason", "message.attributes.reasonPhrase");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(appModel, "org.mule.extension.http.api.HttpResponseAttributes", expressionsPerProperty,
                           "message.attributes.headers mapObject ((value, key, index) -> { (if(upper(key as String) startsWith 'X-MULE_') upper((key as String) [2 to -1]) else key) : value })");
    } catch (IOException e) {
      throw new RuntimeException(e);
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
    return setText(new Element("headers", httpNamespace), "#[migration::HttpRequester::httpRequesterHeaders(vars)]");
  }

  public static void httpRequesterLib(ApplicationModel appModel) {
    try {
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "HttpRequester.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the request headers building logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpRequesterHeaders(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)http\\..*|Connection|Host|Transfer-Encoding/" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars.compatibility_outboundProperties filterObject" + lineSeparator() +
                  "        ((value,key) -> not ((key as String) matches matcher_regex))" + lineSeparator() +
                  "        mapObject ((value, key, index) -> {" + lineSeparator() +
                  "            (if (upper(key as String) startsWith 'MULE_') upper('X-' ++ key as String) else key) : value"
                  + lineSeparator() +
                  "        })" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Emulates the request headers building logic of the Mule 3.x HTTP Transport." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpRequesterTransportHeaders(vars: {}) = do {" + lineSeparator() +
                  "    var matcher_regex = /(?i)http\\..*|Connection|Host|Transfer-Encoding|"
                  + "Accept-Ranges|Age|Content-Disposition|Set-Cookie|ETag|Location|"
                  + "Proxy-Authenticate|Retry-After|Server|Vary|WWW-Authenticate/"
                  + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars.compatibility_outboundProperties filterObject" + lineSeparator() +
                  "        ((value,key) -> not ((key as String) matches matcher_regex))" + lineSeparator() +
                  "        mapObject ((value, key, index) -> {" + lineSeparator() +
                  "            (if (upper(key as String) startsWith 'MULE_') upper('X-' ++ key as String) else key) : value"
                  + lineSeparator() +
                  "        })" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Emulates the request method logic of the Mule 3.x HTTP Connector." + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun httpRequesterMethod(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties['http.method'] default 'POST'" + lineSeparator() +
                  "}" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleReferencedRequestBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      Element builder =
          getApplicationModel()
              .getNode("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='request-builder' and @name='"
                  + builderRef.getAttributeValue("ref") + "']");

      handleReferencedRequestBuilder(builder, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());
      builder.getParent().removeContent(builder);

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
    }
  }
}
