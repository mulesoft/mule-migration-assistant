/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the listener configuration of the HTTP Connector
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListenerConfig extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR = "/mule:mule/http:listener-config";

  @Override
  public String getDescription() {
    return "Update HTTP Connector listener config.";
  }

  public HttpConnectorListenerConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace httpNamespace = Namespace.getNamespace("http", HTTP_NAMESPACE);
    object.setNamespace(httpNamespace);

    if ("listener-config".equals(object.getName()) && object.getChild("listener-connection", httpNamespace) == null) {
      final Element listenerConnection = new Element("listener-connection", httpNamespace);
      copyAttributeIfPresent(object, listenerConnection, "protocol");
      copyAttributeIfPresent(object, listenerConnection, "host");
      copyAttributeIfPresent(object, listenerConnection, "port");
      copyAttributeIfPresent(object, listenerConnection, "usePersistentConnections");
      copyAttributeIfPresent(object, listenerConnection, "connectionIdleTimeout");
      copyAttributeIfPresent(object, listenerConnection, "tlsContext-ref", "tlsContext");

      if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
        report.report(WARN, object, object,
                      "'parseRequest' is not needed in Mule 4, since the InputStream of the multipart payload is provided at it is read.",
                      "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-connectors-http#http-mime-types",
                      "https://docs.mulesoft.com/mule-user-guide/v/4.1/dataweave-formats#format_form_data");
      }
      object.addContent(listenerConnection);
    }


    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE.equals(c.getNamespaceURI())) {
        execute(c, report);
      } else if (TLS_NAMESPACE_URI.equals(c.getNamespaceURI()) && "context".equals(c.getName())) {
        final Element listenerConnection = c.getParentElement().getChild("listener-connection", httpNamespace);
        c.getParentElement().removeContent(c);
        listenerConnection.addContent(c);
      }
    });

    if ("worker-threading-profile".equals(object.getName())) {
      report.report(WARN, object, object.getParentElement(),
                    "Threading profiles do not exist in Mule 4. This may be replaced by a 'maxConcurrency' value in the flow.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-engine");
      object.getParentElement().removeContent(object);
    }
  }

}
