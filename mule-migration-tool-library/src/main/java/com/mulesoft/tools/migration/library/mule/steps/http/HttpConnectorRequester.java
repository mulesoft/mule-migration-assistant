/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates the requester operation of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorRequester extends AbstractApplicationModelMigrationStep {

  private static final String CORE_NAMESPACE = "http://www.mulesoft.org/schema/mule/core";
  private static final String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http";
  private static final String COMPATIBILITY_NAMESPACE = "http://www.mulesoft.org/schema/mule/compatibility";

  public static final String XPATH_SELECTOR = "/mule:mule//http:request";

  @Override
  public String getDescription() {
    return "Update HTTP requester operation.";
  }

  public HttpConnectorRequester() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    if (object.getAttribute("port") != null) {
      report.report(ERROR, object, object,
                    "'port' cannot be overriden at the HTTP request operation. You may use an expression in the config to make it dynamic.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-connectors-http#http-request");
      object.removeAttribute("port");
    }
    if (object.getAttribute("host") != null) {
      report.report(ERROR, object, object,
                    "'host' cannot be overriden at the HTTP request operation. You may use an expression in the config to make it dynamic.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-connectors-http#http-request");
      object.removeAttribute("host");
    }

    if (object.getAttribute("path") != null) {
      object.setAttribute("path", getExpressionMigrator().migrateExpression(object.getAttributeValue("path")));
    }
    if (object.getAttribute("method") != null) {
      object.setAttribute("method", getExpressionMigrator().migrateExpression(object.getAttributeValue("method")));
    }
    if (object.getAttribute("followRedirects") != null) {
      object.setAttribute("followRedirects",
                          getExpressionMigrator().migrateExpression(object.getAttributeValue("followRedirects")));
    }
    if (object.getAttribute("target") != null) {
      object.setAttribute("target", getExpressionMigrator().migrateExpression(object.getAttributeValue("target")));
    }

    addAttributesToInboundProperties(object, report);

    addOutboundPropertiesToVariable(object, report);

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        executeChild(c, report, httpNamespace);
      }
    });

    if (object.getAttribute("source") != null) {
      if (!"#[payload]".equals(object.getAttributeValue("source"))) {
        object.addContent(new Element("body", httpNamespace)
            .setText(getExpressionMigrator().migrateExpression(object.getAttributeValue("source"))));
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

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    getApplicationModel().addNameSpace(Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE),
                                       "http://www.mulesoft.org/schema/mule/compatibility/current/mule-compatibility.xsd",
                                       object.getDocument());

    int index = object.getParent().indexOf(object);
    Element a2ip = new Element("attributes-to-inbound-properties",
                               Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE));
    object.getParent().addContent(index + 1, a2ip);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.status", "message.attributes.statusCode");
    expressionsPerProperty.put("http.reason", "message.attributes.reasonPhrase");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.http.api.HttpRequestAttributes", expressionsPerProperty);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    report.report(WARN, a2ip, a2ip,
                  "Expressions that query inboundProperties from the message should instead query the attributes of the message."
                      + lineSeparator()
                      + "Remove this component when there are no remaining usages of inboundProperties in expressions or components that rely on inboundProperties (such as copy-properties)",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#inbound-properties-are-now-attributes");
  }

  private void addOutboundPropertiesToVariable(Element object, MigrationReport report) {
    int index = object.getParent().indexOf(object);

    Element setVariable = new Element("set-variable", Namespace.getNamespace(CORE_NAMESPACE));
    setVariable.setAttribute("variableName", "compatibility_outboundProperties");
    setVariable.setAttribute("value", "#[mel:message.outboundProperties]");
    object.getParent().addContent(index, setVariable);

    report.report(WARN, setVariable, setVariable,
                  "Instead of setting outbound properties in the flow, its values must be set explicitly in the operation/listener.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
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
    return new Element("headers", httpNamespace).setAttribute("expression", "flowVars.compatibility_outboundProperties");
  }

  private void handleReferencedRequestBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    int idx = 0;
    while (builderRef != null) {

      object.removeContent(builderRef);

      String xPathQuery = "/mule:mule/http:request-builder[@name='" + builderRef.getAttributeValue("ref") + "']";
      Element builder = getElementsFromDocument(object.getDocument(), xPathQuery).get(0);

      handleReferencedRequestBuilder(builder, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());
      builder.getParent().removeContent(builder);

      object.addContent(idx, builderContent);
      idx += builderContent.size();

      builderRef = object.getChild("builder", httpNamespace);
    }
  }

  // TODO Move
  public static List<Element> getElementsFromDocument(Document doc, String xPathExpression) {
    List<Namespace> namespaces = new ArrayList<>();
    namespaces.add(Namespace.getNamespace("mule", doc.getRootElement().getNamespace().getURI()));
    namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
    List<Element> nodes = xpath.evaluate(doc);
    return nodes;
  }
}
