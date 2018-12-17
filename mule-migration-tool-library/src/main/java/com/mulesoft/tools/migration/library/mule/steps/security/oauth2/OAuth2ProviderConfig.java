/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.oauth2;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpInboundEndpoint.extractListenerConfig;
import static com.mulesoft.tools.migration.library.mule.steps.http.HttpsInboundEndpoint.handleHttpsListenerConfig;
import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringBeans.SPRING_BEANS_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Update oauth2 provider configuration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OAuth2ProviderConfig extends AbstractApplicationModelMigrationStep {

  public static final String OAUTH2_PROVIDER_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/oauth2-provider";
  public static final Namespace OAUTH2_PROVIDER_NAMESPACE = getNamespace("oauth2-provider", OAUTH2_PROVIDER_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + OAUTH2_PROVIDER_NAMESPACE_URI + "' and local-name() = 'config']";

  @Override
  public String getDescription() {
    return "Update oauth2 provider configuration.";
  }

  public OAuth2ProviderConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(OAUTH2_PROVIDER_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(OAUTH2_PROVIDER_NAMESPACE,
                                       "http://www.mulesoft.org/schema/mule/oauth2-provider/current/mule-oauth2-provider.xsd",
                                       element.getDocument());

    if (element.getAttribute("preFlow-ref") != null) {
      report.report("oauth2Provider.preFlow", element, element);
      element.removeAttribute("preFlow-ref");
    }

    if (element.getAttribute("providerName") != null) {
      element.getAttribute("providerName").setName("name");
    } else if (element.getAttribute("name") == null) {
      element.setAttribute("name", "oauth2ProviderConfig");
    }

    if (element.getAttribute("clientStore-ref") != null) {
      getApplicationModel().getNodeOptional("//*[namespace-uri() = '" + SPRING_BEANS_NS_URI
          + "' and local-name() = 'bean' and @name='" + element.getAttributeValue("clientStore-ref") + "']")
          .ifPresent(b -> {
            element.getAttribute("clientStore-ref").setValue(b.getAttributes().stream()
                .filter(att -> "objectStore-ref".equals(att.getName())).map(att -> att.getValue()).findFirst().get());
          });

      element.getAttribute("clientStore-ref").setName("clientStore");
    }

    if (element.getAttribute("listenerConfig-ref") != null) {
      element.getAttribute("listenerConfig-ref").setName("listenerConfig");
    } else if (element.getAttribute("connector-ref") != null) {
      String configName = element.getAttributeValue("connector-ref");

      final String port = element.getAttributeValue("port");
      element.removeAttribute("port");

      final Element httpConnector = getHttpConnector(element.getAttributeValue("connector-ref"));
      extractListenerConfig(getApplicationModel(), element, () -> httpConnector, HTTP_NAMESPACE, configName, "localhost",
                            port != null ? port : "9999");
      getApplicationModel().addNameSpace("http", HTTP_NAMESPACE_URI,
                                         "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
      if (HTTPS_NAMESPACE.equals(httpConnector.getNamespace())) {
        handleHttpsListenerConfig(getApplicationModel(), element, report, httpConnector);
      }
      element.removeAttribute("config-ref");

      element.getAttribute("connector-ref").setName("listenerConfig");
    } else {
      final String httpListenerConfigName = element.getAttributeValue("name") + "_httpListenerConfig";

      element.setAttribute("listenerConfig", httpListenerConfigName);

      final String port = element.getAttributeValue("port");
      element.removeAttribute("port");

      addElementAfter(new Element("listener-config", HTTP_NAMESPACE)
          .setAttribute("name", httpListenerConfigName)
          .addContent(new Element("listener-connection", HTTP_NAMESPACE)
              .setAttribute("host", "localhost")
              .setAttribute("port", port != null ? port : "9999")), element);
    }
    element.removeAttribute("port");

    element.getAttribute("resourceOwnerSecurityProvider-ref").setName("resourceOwnerSecurityProvider");
    element.getAttribute("clientSecurityProvider-ref").setName("clientSecurityProvider");

    final String scopes = element.getAttributeValue("scopes");
    if (scopes != null) {
      element.setAttribute("scopes", stream(scopes.split(" ")).collect(joining(",")));
    }

    final String defaultScopes = element.getAttributeValue("defaultScopes");
    if (defaultScopes != null) {
      element.setAttribute("defaultScopes", stream(defaultScopes.split(" ")).collect(joining(",")));
    }

    final String supportedGrantTypes = element.getAttributeValue("supportedGrantTypes");
    if (supportedGrantTypes != null) {
      element.setAttribute("supportedGrantTypes", stream(supportedGrantTypes.split(" ")).collect(joining(",")));
    }

    if (element.getAttribute("rateLimiter-ref") != null) {
      migrateRateLimiter(element, report);
    }

    migrateTokenConfig(element, report);
    migrateAuthorizationConfig(element, report);
    migrateClients(element);
  }

  private void migrateRateLimiter(Element element, MigrationReport report) {
    final Element clientValidationRateLimiter = new Element("client-validation-rate-limiter", OAUTH2_PROVIDER_NAMESPACE);
    final Element periodRateLimiter = new Element("period-rate-limiter", OAUTH2_PROVIDER_NAMESPACE);

    clientValidationRateLimiter.addContent(periodRateLimiter);
    element.addContent(clientValidationRateLimiter);

    getApplicationModel().getNodeOptional("//*[namespace-uri() = '" + SPRING_BEANS_NS_URI
        + "' and local-name() = 'bean' and @name='" + element.getAttributeValue("rateLimiter-ref") + "']").ifPresent(b -> {
          report.report("oauth2Provider.clientValidationRateLimiter", b, clientValidationRateLimiter);
        });

    element.removeAttribute("rateLimiter-ref");
  }

  private void migrateTokenConfig(Element element, MigrationReport report) {
    final Element tokenConfig = new Element("token-config", OAUTH2_PROVIDER_NAMESPACE);

    final String path = element.getAttributeValue("accessTokenEndpointPath");
    if (path != null) {
      tokenConfig.setAttribute("path", path.startsWith("/") ? path : "/" + path);
      element.removeAttribute("accessTokenEndpointPath");
    }

    if (element.getAttributeValue("tokenStore-ref") != null) {
      AtomicReference<Element> refreshTokens = new AtomicReference<>();
      if ("true".equals(element.getAttributeValue("enableRefreshToken"))) {
        refreshTokens.set(new Element("single-refresh-tokens", OAUTH2_PROVIDER_NAMESPACE));
        tokenConfig.addContent(new Element("refresh-token-strategy", OAUTH2_PROVIDER_NAMESPACE)
            .addContent(refreshTokens.get().setAttribute("objectStore", "lalala")));


        if (element.getAttributeValue("refreshTokenTtlSeconds") != null) {
          report.report("oauth2Provider.refreshTokenTtl", element, tokenConfig);
          element.removeAttribute("refreshTokenTtlSeconds");
        }
      }
      element.removeAttribute("enableRefreshToken");

      getApplicationModel().getNodeOptional("//*[namespace-uri() = '" + SPRING_BEANS_NS_URI
          + "' and local-name() = 'bean' and @name='" + element.getAttributeValue("tokenStore-ref") + "']")
          .ifPresent(b -> {
            tokenConfig.setAttribute("tokenStore", b.getAttributes().stream()
                .filter(att -> "accessTokenObjectStore-ref".equals(att.getName())).map(att -> att.getValue()).findFirst().get());

            if (refreshTokens.get() != null) {
              refreshTokens.get().setAttribute("objectStore", b.getAttributes().stream()
                  .filter(att -> "refreshTokenObjectStore-ref".equals(att.getName())).map(att -> att.getValue()).findFirst()
                  .get());

            }
          });

      element.removeAttribute("tokenStore-ref");

      if (element.getAttributeValue("tokenTtlSeconds") != null) {
        report.report("oauth2Provider.tokenTtl", element, tokenConfig);
        element.removeAttribute("tokenTtlSeconds");
      }
    }

    element.addContent(tokenConfig);
  }

  private void migrateAuthorizationConfig(Element element, MigrationReport report) {
    final Element authorizationConfig = new Element("authorization-config", OAUTH2_PROVIDER_NAMESPACE);

    if (element.getAttributeValue("loginPage") != null) {
      authorizationConfig.setAttribute("loginPage", element.getAttributeValue("loginPage"));
      element.removeAttribute("loginPage");
    }
    final String path = element.getAttributeValue("authorizationEndpointPath");
    if (path != null) {
      authorizationConfig.setAttribute("path", path.startsWith("/") ? path : "/" + path);
      element.removeAttribute("authorizationEndpointPath");
    }

    if (element.getAttributeValue("authorizationCodeStore-ref") != null) {
      getApplicationModel().getNodeOptional("//*[namespace-uri() = '" + SPRING_BEANS_NS_URI
          + "' and local-name() = 'bean' and @name='" + element.getAttributeValue("authorizationCodeStore-ref") + "']")
          .ifPresent(b -> {
            authorizationConfig.setAttribute("authorizationCodeStore", b.getAttributes().stream()
                .filter(att -> "objectStore-ref".equals(att.getName())).map(att -> att.getValue()).findFirst().get());
          });

      element.removeAttribute("authorizationCodeStore-ref");

      if (element.getAttributeValue("authorizationTtlSeconds") != null) {
        report.report("oauth2Provider.authorizationTtl", element, authorizationConfig);
        element.removeAttribute("authorizationTtlSeconds");
      }
    }

    element.addContent(authorizationConfig);
  }

  private void migrateClients(Element element) {
    final Element clients = element.getChild("clients", OAUTH2_PROVIDER_NAMESPACE);
    if (clients != null) {
      clients.detach();
      element.addContent(clients);

      clients.getChildren("client", OAUTH2_PROVIDER_NAMESPACE).forEach(client -> {
        client.getChild("redirect-uris", OAUTH2_PROVIDER_NAMESPACE).setName("client-redirect-uris")
            .getChildren("redirect-uri", OAUTH2_PROVIDER_NAMESPACE)
            .forEach(redirectUri -> {
              redirectUri.setAttribute("value", redirectUri.getTextTrim());
              redirectUri.setName("client-redirect-uri");
              redirectUri.removeContent();
            });

        client.getChild("authorized-grant-types", OAUTH2_PROVIDER_NAMESPACE).setName("client-authorized-grant-types")
            .getChildren("authorized-grant-type", OAUTH2_PROVIDER_NAMESPACE)
            .forEach(redirectUri -> {
              redirectUri.setAttribute("value", redirectUri.getTextTrim());
              redirectUri.setName("client-authorized-grant-type");
              redirectUri.removeContent();
            });
      });
    }
  }

  protected Element getHttpConnector(String connectorName) {
    return getApplicationModel().getNodeOptional("/*/http:connector[@name = '" + connectorName + "']")
        .orElse(getApplicationModel().getNode("/*/https:connector[@name = '" + connectorName + "']"));
  }

}
