/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.engine.step.AbstractApplicationModelMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the listener configuration of the HTTP Connector
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListenerConfig extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NAMESPACE = "http://www.mulesoft.org/schema/mule/http";
  private static final String TLS_NAMESPACE = "http://www.mulesoft.org/schema/mule/tls";

  public static final String XPATH_SELECTOR = "/*/*[namespace-uri()='" + HTTP_NAMESPACE + "'"
      + " and local-name()='listener-config']";

  @Override
  public String getDescription() {
    return "Update HTTP Connector listener config.";
  }

  public HttpConnectorListenerConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    if ("listener-config".equals(object.getName())) {
      final Element listenerConnection = new Element("listener-connection", httpNamespace);
      copyAttributeIfPresent(object, listenerConnection, "protocol");
      copyAttributeIfPresent(object, listenerConnection, "host");
      copyAttributeIfPresent(object, listenerConnection, "port");
      copyAttributeIfPresent(object, listenerConnection, "usePersistentConnections");
      copyAttributeIfPresent(object, listenerConnection, "connectionIdleTimeout");
      copyAttributeIfPresent(object, listenerConnection, "tlsContext-ref", "tlsContext");

      if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
        // TODO WARN
      }
      object.addContent(listenerConnection);
    }


    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        execute(c);
      } else if (TLS_NAMESPACE.equals(c.getNamespaceURI()) && "context".equals(c.getName())) {
        final Element listenerConnection = c.getParentElement().getChild("listener-connection", httpNamespace);
        c.getParentElement().removeContent(c);
        listenerConnection.addContent(c);
      }
    });

    if ("worker-threading-profile".equals(object.getName())) {
      // TODO Change into maxConcurrency/backpressure config?
      // TODO WARN
      object.getParentElement().removeContent(object);
    }
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
