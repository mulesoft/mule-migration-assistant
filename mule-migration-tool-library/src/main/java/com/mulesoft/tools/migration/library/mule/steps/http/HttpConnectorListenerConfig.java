/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.ArrayList;

/**
 * Migrates the listener configuration of the HTTP Connector
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListenerConfig extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='listener-config']";

  @Override
  public String getDescription() {
    return "Update HTTP Connector listener config.";
  }

  public HttpConnectorListenerConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(asList(HTTP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(HTTP_NAMESPACE);

    if ("listener-config".equals(object.getName()) && object.getChild("listener-connection", HTTP_NAMESPACE) == null) {
      final Element listenerConnection = new Element("listener-connection", HTTP_NAMESPACE);
      copyAttributeIfPresent(object, listenerConnection, "protocol");
      copyAttributeIfPresent(object, listenerConnection, "host");
      copyAttributeIfPresent(object, listenerConnection, "port");
      copyAttributeIfPresent(object, listenerConnection, "usePersistentConnections");
      copyAttributeIfPresent(object, listenerConnection, "connectionIdleTimeout");
      copyAttributeIfPresent(object, listenerConnection, "tlsContext-ref", "tlsContext");

      if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
        report.report("http.parseRequest", object, object);
      }
      object.addContent(listenerConnection);
    }


    new ArrayList<>(object.getChildren()).forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI()) && "worker-threading-profile".equals(c.getName())) {
        report.report("flow.threading", c, c.getParentElement());
        c.getParentElement().removeContent(c);
      } else if (TLS_NAMESPACE_URI.equals(c.getNamespaceURI()) && "context".equals(c.getName())) {
        final Element listenerConnection = c.getParentElement().getChild("listener-connection", HTTP_NAMESPACE);
        c.getParentElement().removeContent(c);
        listenerConnection.addContent(c);
      }
    });

  }

}
