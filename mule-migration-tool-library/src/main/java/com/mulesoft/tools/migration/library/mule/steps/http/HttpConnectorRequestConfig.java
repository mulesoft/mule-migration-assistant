/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the request configuration of the HTTP Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorRequestConfig extends AbstractHttpConnectorMigrationStep {

  private static final String TCP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/tcp";
  private static final String TCP_NAMESPACE_PREFIX = "tcp";
  private static final Namespace TCP_NAMESPACE = Namespace.getNamespace(TCP_NAMESPACE_PREFIX, TCP_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = ""
      + "/*/*["
      + "namespace-uri()='" + HTTP_NAMESPACE_URI + "' and ("
      + " local-name()='request-config' or"
      + " local-name()='proxy' or"
      + " local-name()='ntlm-proxy'"
      + ")]";

  @Override
  public String getDescription() {
    return "Update HTTP Connector request config.";
  }

  public HttpConnectorRequestConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(TLS_NAMESPACE, TCP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(HTTP_NAMESPACE);

    if ("request-config".equals(object.getName())) {
      final Element requestConnection = new Element("request-connection", HTTP_NAMESPACE);
      copyAttributeIfPresent(object, requestConnection, "protocol");
      copyExpressionAttributeIfPresent(object, requestConnection, "host", "host", true);
      copyExpressionAttributeIfPresent(object, requestConnection, "port", "port", true);
      copyAttributeIfPresent(object, requestConnection, "usePersistentConnections");
      copyAttributeIfPresent(object, requestConnection, "maxConnections");
      copyAttributeIfPresent(object, requestConnection, "connectionIdleTimeout");
      copyAttributeIfPresent(object, requestConnection, "streamResponse");
      copyAttributeIfPresent(object, requestConnection, "responseBufferSize");
      copyAttributeIfPresent(object, requestConnection, "tlsContext-ref", "tlsContext");
      copyAttributeIfPresent(object, requestConnection, "clientSocketProperties-ref", "clientSocketProperties");
      copyAttributeIfPresent(object, requestConnection, "proxy-ref", "proxyConfig");

      object.addContent(requestConnection);

      for (Attribute attribute : object.getAttributes()) {
        if ("basePath".equals(attribute.getName())
            || "followRedirects".equals(attribute.getName())
            || "sendBodyMode".equals(attribute.getName())
            || "requestStreamingMode".equals(attribute.getName())
            || "responseTimeout".equals(attribute.getName())) {
          attribute.setValue(getExpressionMigrator().migrateExpression(attribute.getValue(), true, object));
        }
      }
    }

    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        execute(c, report);
      } else if (TLS_NAMESPACE_URI.equals(c.getNamespaceURI()) && "context".equals(c.getName())) {
        final Element requestConnection = c.getParentElement().getChild("request-connection", HTTP_NAMESPACE);
        c.detach();
        requestConnection.addContent(c);
      } else if (TCP_NAMESPACE_URI.equals(c.getNamespaceURI()) && "client-socket-properties".equals(c.getName())) {
        final Element clientSocketPropsContainer = new Element("client-socket-properties", HTTP_NAMESPACE);
        final Element requestConnection = c.getParentElement().getChild("request-connection", HTTP_NAMESPACE);

        c.detach();
        clientSocketPropsContainer.addContent(c);
        requestConnection.addContent(clientSocketPropsContainer);
      }

    });

    if ("basic-authentication".equals(object.getName())
        || "digest-authentication".equals(object.getName())
        || "ntlm-authentication".equals(object.getName())) {
      final Element authentication = new Element("authentication", HTTP_NAMESPACE);
      final Element requestConnection = object.getParentElement().getChild("request-connection", HTTP_NAMESPACE);

      object.detach();
      authentication.addContent(object);
      requestConnection.addContent(authentication);

      for (Attribute attribute : object.getAttributes()) {
        XmlDslUtils.migrateExpression(attribute, getExpressionMigrator());
      }
    }

    if (("proxy".equals(object.getName())
        || "ntlm-proxy".equals(object.getName()))
        && "request-config".equals(object.getParentElement().getName())) {
      final Element proxyConfig = new Element("proxy-config", HTTP_NAMESPACE);
      final Element requestConnection = object.getParentElement().getChild("request-connection", HTTP_NAMESPACE);

      object.detach();
      proxyConfig.addContent(object);
      requestConnection.addContent(proxyConfig);
    }

    if ("raml-api-configuration".equals(object.getName())) {
      report.report("http.restConnect", object, object.getParentElement());
      object.detach();
    }
  }

  protected void copyExpressionAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                                  final String targetAttributeName, boolean expression) {
    if (source.getAttribute(sourceAttributeName) != null) {
      String sourceAttributeValue = source.getAttributeValue(sourceAttributeName);
      String value = sourceAttributeValue;
      if (expression && getExpressionMigrator().isWrapped(sourceAttributeValue)) {
        value = getExpressionMigrator().migrateExpression(sourceAttributeValue, true, target);
      }
      target.setAttribute(targetAttributeName, value);
      source.removeAttribute(sourceAttributeName);
    }
  }
}
