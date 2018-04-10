/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static java.util.Collections.emptyList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrates the listener source of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListener extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http";

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

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        execute(c, report);
      }
    });

    if ("response-builder".equals(object.getName())) {
      handleReferencedResponseBuilder(object, httpNamespace);

      object.setName("response");
    }
    if ("error-response-builder".equals(object.getName())) {
      handleReferencedResponseBuilder(object, httpNamespace);

      object.setName("error-response");
    }
  }

  private void handleReferencedResponseBuilder(Element object, final Namespace httpNamespace) {
    Element builderRef = object.getChild("builder", httpNamespace);
    if (builderRef != null) {

      object.removeContent(builderRef);

      String xPathQuery = "/mule:mule/http:response-builder[@name='" + builderRef.getAttributeValue("ref") + "']";
      Element builder = getElementsFromDocument(object.getDocument(), xPathQuery).get(0);

      handleReferencedResponseBuilder(builder, httpNamespace);
      List<Element> builderContent = ImmutableList.copyOf(builder.getChildren()).asList();
      builder.setContent(emptyList());
      builder.getParent().removeContent(builder);

      object.addContent(0, builderContent);
    }
  }

  public static List<Element> getElementsFromDocument(Document doc, String xPathExpression) {
    List<Namespace> namespaces = new ArrayList<>();
    namespaces.add(Namespace.getNamespace("mule", doc.getRootElement().getNamespace().getURI()));
    namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
    List<Element> nodes = xpath.evaluate(doc);
    return nodes;
  }

  protected void copyAttributeIfPresent(final Element source, final Element target, final String attributeName) {
    copyAttributeIfPresent(source, target, attributeName, attributeName);
  }

  protected void copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                        final String targetAttributeName) {
    if (source.getAttribute(sourceAttributeName) != null) {
      target.setAttribute(targetAttributeName, source.getAttributeValue(sourceAttributeName));
      source.removeAttribute(sourceAttributeName);
    }
  }

}
