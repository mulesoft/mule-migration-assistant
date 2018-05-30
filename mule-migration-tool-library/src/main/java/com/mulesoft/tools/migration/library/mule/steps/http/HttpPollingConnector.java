/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates the polling connector of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpPollingConnector extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/mule:mule/http:polling-connector";

  @Override
  public String getDescription() {
    return "Update HTTP polling connector.";
  }

  public HttpPollingConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace httpNamespace = Namespace.getNamespace("http", "http://www.mulesoft.org/schema/mule/http");

    Element requestConnection = new Element("request-connection", httpNamespace);
    String configName = object.getAttributeValue("name") + "Config";
    object.getParentElement().addContent(1, new Element("request-config", httpNamespace)
        .setAttribute("name", configName)
        .addContent(requestConnection));

    if (object.getAttribute("reuseAddress") != null) {
      report.report(ERROR, object, object, "'reuseAddress' attribute is onli applicable to HTTP listeners, not requesters.");
      object.removeAttribute("reuseAddress");
    }

    List<Element> pollingEndpoints =
        getApplicationModel().getNodes("//*[@connector-ref = '" + object.getAttributeValue("name") + "']");

    for (Element pollingEndpoint : pollingEndpoints) {
      Element requestOperation = new Element("request", httpNamespace);
      requestOperation.setAttribute("path", "/");

      processAddress(pollingEndpoint, report).ifPresent(address -> {
        requestConnection.setAttribute("host", address.getHost());
        requestConnection.setAttribute("port", address.getPort());
        if (address.getPath() != null) {
          requestOperation.setAttribute("path", address.getPath());
        }

        if (address.getCredentials() != null) {
          String[] credsSplit = address.getCredentials().split("@");

          Element basicAuth = getBasicAuth(requestConnection, httpNamespace);
          basicAuth.setAttribute("username", credsSplit[0]);
          basicAuth.setAttribute("password", credsSplit[1]);
        }
      });
      copyAttributeIfPresent(pollingEndpoint, requestConnection, "host");
      copyAttributeIfPresent(pollingEndpoint, requestConnection, "port");
      copyAttributeIfPresent(pollingEndpoint, requestOperation, "path");

      if (pollingEndpoint.getAttribute("user") != null || pollingEndpoint.getAttribute("password") != null) {
        Element basicAuth = getBasicAuth(requestConnection, httpNamespace);

        copyAttributeIfPresent(pollingEndpoint, basicAuth, "user", "username");
        copyAttributeIfPresent(pollingEndpoint, basicAuth, "password");
      }

      requestOperation.setAttribute("config-ref", configName);

      Element pollingSource = new Element("scheduler", CORE_NAMESPACE)
          .addContent(new Element("scheduling-strategy", CORE_NAMESPACE)
              .addContent(new Element("fixed-frequency", CORE_NAMESPACE)
                  .setAttribute("frequency", changeDefault("1000", "60000", object.getAttributeValue("pollingFrequency")))));

      // TODO checkEtag
      // TODO discardEmptyContent

      for (Element prop : pollingEndpoint.getChildren("property", CORE_NAMESPACE)) {
        requestOperation.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", prop.getAttributeValue("key"))
            .setAttribute("value", prop.getAttributeValue("value")));
      }

      addAttributesToInboundProperties(pollingEndpoint, report);
      pollingEndpoint.getParentElement().addContent(0, asList(pollingSource, requestOperation));
      pollingEndpoint.detach();
    }

    object.detach();
  }

  private Element getBasicAuth(Element requestConnection, Namespace httpNamespace) {
    Element auth = requestConnection.getChild("authentication", httpNamespace);
    Element basicAuth;
    if (auth != null) {
      basicAuth = auth.getChild("basic-authentication", httpNamespace);
      if (basicAuth == null) {
        basicAuth = new Element("basic-authentication", httpNamespace);
        auth.addContent(basicAuth);
      }
    } else {
      basicAuth = new Element("basic-authentication", httpNamespace);
      requestConnection.addContent(new Element("authentication", httpNamespace)
          .addContent(basicAuth));
    }

    return basicAuth;
  }

  private void addAttributesToInboundProperties(Element object, MigrationReport report) {
    migrateInboundEndpointStructure(getApplicationModel(), object, report, false);

    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("http.status", "message.attributes.statusCode");
    expressionsPerProperty.put("http.reason", "message.attributes.reasonPhrase");
    expressionsPerProperty.put("http.headers", "message.attributes.headers");

    try {
      addAttributesMapping(getApplicationModel(), "org.mule.extension.http.api.HttpResponseAttributes", expressionsPerProperty);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
