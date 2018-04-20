/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getElementsFromDocument;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateSourceStructure;
import static java.util.Collections.emptyList;

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
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
      report.report(WARN, object, object,
                    "'parseRequest' is not needed in Mule 4, since the InputStream of the multipart payload is provided at it is read.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-connectors-http#http-mime-types",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/dataweave-formats#format_form_data");
    }
    object.removeAttribute("parseRequest");

    addAttributesToInboundProperties(object, report);

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if (object.getChild("response", httpNamespace) == null) {
      object.addContent(new Element("response", httpNamespace).addContent(compatibilityHeaders(httpNamespace)));
    }
    if (object.getChild("error-response", httpNamespace) == null) {
      object.addContent(new Element("error-response", httpNamespace).addContent(compatibilityHeaders(httpNamespace)));
    }
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateSourceStructure(getApplicationModel(), object, report);

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

  public void executeChild(Element object, MigrationReport report, Namespace httpNamespace) throws RuntimeException {
    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if ("response-builder".equals(object.getName())) {
      handleReferencedResponseBuilder(object, httpNamespace);
      object.addContent(compatibilityHeaders(httpNamespace));

      object.setName("response");
    }
    if ("error-response-builder".equals(object.getName())) {
      handleReferencedResponseBuilder(object, httpNamespace);
      object.addContent(compatibilityHeaders(httpNamespace));

      object.setName("error-response");
    }
  }

  private Element compatibilityHeaders(Namespace httpNamespace) {
    return new Element("headers", httpNamespace)
        .setText("#[vars.compatibility_outboundProperties filterObject ((value,key) -> not ((key as String) matches /http\\..*|Connection|Transfer-Encoding/i))]");
  }

  private void handleReferencedResponseBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      Element builder =
          getElementsFromDocument(object.getDocument(),
                                  "/mule:mule/http:response-builder[@name='" + builderRef.getAttributeValue("ref") + "']").get(0);

      handleReferencedResponseBuilder(builder, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());
      builder.getParent().removeContent(builder);

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
    }
  }
}
