/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester.addAttributesToInboundProperties;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.VALIDATION_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationMigration.addValidationNamespace;
import static com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution.addValidationDependency;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_POLICY;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateInboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Migrates the polling connector of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpPollingConnector extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR = "/*/http:polling-connector";

  private ExpressionMigrator expressionMigrator;

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
      report.report(ERROR, object, object, "'reuseAddress' attribute is only applicable to HTTP listeners, not requesters.");
      object.removeAttribute("reuseAddress");
    }

    if (MULE_FOUR_POLICY.equals(getApplicationModel().getProjectType())) {
      report.report(ERROR, requestConnection, object,
                    "The configuration for this connector was put in the endpoints in Mule 3. Complete this connection provider in the domain with the appropriate configuration.");
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

      if (object.getAttribute("checkEtag") == null || "true".equals(object.getAttributeValue("checkEtag"))) {
        Element etagValidator = new Element("choice", CORE_NAMESPACE).addContent(new Element("when", CORE_NAMESPACE)
            .setAttribute("expression", "#[message.attributes.headers.ETag != null]")
            .addContent(new Element("idempotent-message-validator", CORE_NAMESPACE)
                .setAttribute("idExpression", "#[message.attributes.headers.ETag]")));

        addElementAfter(etagValidator, pollingEndpoint);
        report.report(ERROR, etagValidator, etagValidator, "Consider using a watermark for checking ETag",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-patterns-watermark");
      }
      if (object.getAttribute("discardEmptyContent") == null || "true".equals(object.getAttributeValue("discardEmptyContent"))) {
        addValidationDependency(getApplicationModel().getPomModel().get());
        addValidationNamespace(object.getDocument());
        addElementAfter(new Element("is-true", VALIDATION_NAMESPACE)
            .setAttribute("expression", "#[(message.attributes.headers['Content-Length'] as Number default -1) != 0]"),
                        pollingEndpoint);
      }

      for (Element prop : pollingEndpoint.getChildren("property", CORE_NAMESPACE)) {
        requestOperation.addContent(new Element("header", httpNamespace)
            .setAttribute("headerName", prop.getAttributeValue("key"))
            .setAttribute("value", prop.getAttributeValue("value")));
      }

      migrateInboundEndpointStructure(getApplicationModel(), pollingEndpoint, report, false);
      addAttributesToInboundProperties(pollingEndpoint, getApplicationModel(), report);
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

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
