/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the jms connector of the JMS transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsConnector extends AbstractApplicationModelMigrationStep {

  private static final String JMS_NAMESPACE_PREFIX = "jms";
  private static final String JMS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/jms";
  private static final Namespace JMS_NAMESPACE = Namespace.getNamespace(JMS_NAMESPACE_PREFIX, JMS_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "/mule:mule/jms:*["
      + "(local-name() = 'activemq-connector' or "
      + "local-name() = 'activemq-xa-connector' or "
      + "local-name() = 'weblogic-connector' or "
      + "local-name() = 'websphere-connector' or "
      + "local-name() = 'connector' or "
      + "local-name() = 'custom-connector')]";

  @Override
  public String getDescription() {
    return "Update JMS connector config.";
  }

  public JmsConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JMS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }

  public static void addConnectionToConfig(final Element m4JmsConfig, final Element m3Connector, ApplicationModel appModel,
                                           MigrationReport report) {
    Element connection;
    switch (m3Connector.getName()) {
      case "activemq-connector":
        connection = addActiveMqConnection(m4JmsConfig, m3Connector);
        break;
      case "activemq-xa-connector":
        connection = addActiveMqConnection(m4JmsConfig, m3Connector);

        Element factoryConfig = connection.getChild("factory-configuration", JMS_NAMESPACE);
        if (factoryConfig == null) {
          factoryConfig = new Element("factory-configuration", JMS_NAMESPACE);
          connection.addContent(factoryConfig);
        }

        factoryConfig.setAttribute("enable-xa", "true");
        break;
      case "connector":
      case "custom-connector":
        report.report(ERROR, m3Connector, m4JmsConfig, "Cannot automatically migrate JMS custom-connector",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#using-a-different-broker");
        connection = new Element("generic-connection", JMS_NAMESPACE);
        m4JmsConfig.addContent(connection);
        break;
      case "websphere-connector":
        // TODO MMT-202
        report.report(ERROR, m3Connector, m4JmsConfig, "IBM MQ Connector should be used to connecto to an IBM MQ broker",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#using-a-different-broker");
      default:
        connection = new Element("generic-connection", JMS_NAMESPACE);
        m4JmsConfig.addContent(connection);
    }

    String m4Specification = changeDefault("1.0.2b", "1.1", m3Connector.getAttributeValue("specification"));
    if (m4Specification != null && m4Specification.equals("1.0.2b")) {
      connection.setAttribute("specification", "JMS_1_0_2b");
    }

    copyAttributeIfPresent(m3Connector, connection, "username");
    copyAttributeIfPresent(m3Connector, connection, "password");
    copyAttributeIfPresent(m3Connector, connection, "clientId");

    if (m3Connector.getAttribute("connectionFactory-ref") != null) {
      Element connFactory =
          appModel.getNode("/mule:mule/*[@name='" + m3Connector.getAttributeValue("connectionFactory-ref") + "']");
      Element defaultCaching = new Element("default-caching", JMS_NAMESPACE);
      copyAttributeIfPresent(connFactory, defaultCaching, "sessionCacheSize");
      copyAttributeIfPresent(connFactory, defaultCaching, "cacheConsumers");
      copyAttributeIfPresent(connFactory, defaultCaching, "cacheProducers");

      connection.addContent(0, new Element("caching-strategy", JMS_NAMESPACE).addContent(defaultCaching));

      connFactory.detach();
    } else {
      connection.addContent(0, new Element("caching-strategy", JMS_NAMESPACE)
          .addContent(new Element("no-caching", JMS_NAMESPACE)));

    }

    if (m3Connector.getAttribute("connectionFactoryJndiName") != null) {
      Element jndiConnFactory = new Element("jndi-connection-factory", JMS_NAMESPACE);

      copyAttributeIfPresent(m3Connector, jndiConnFactory, "connectionFactoryJndiName");

      Element nameResolverBuilder = new Element("name-resolver-builder", JMS_NAMESPACE);
      copyAttributeIfPresent(m3Connector, nameResolverBuilder, "jndiInitialFactory", "jndiInitialContextFactory");
      copyAttributeIfPresent(m3Connector, nameResolverBuilder, "jndiProviderUrl");
      copyAttributeIfPresent(m3Connector, nameResolverBuilder, "jndiProviderUrl");
      processProviderProperties(m3Connector, appModel, nameResolverBuilder);

      Element m3defaultJndiNameResolver = m3Connector.getChild("default-jndi-name-resolver", JMS_NAMESPACE);
      if (m3defaultJndiNameResolver != null) {
        copyAttributeIfPresent(m3defaultJndiNameResolver, nameResolverBuilder, "jndiInitialFactory", "jndiInitialContextFactory");
        copyAttributeIfPresent(m3defaultJndiNameResolver, nameResolverBuilder, "jndiProviderUrl");
        processProviderProperties(m3defaultJndiNameResolver, appModel, nameResolverBuilder);
      }

      Element m3customJndiNameResolver = m3Connector.getChild("custom-jndi-name-resolver", JMS_NAMESPACE);
      if (m3customJndiNameResolver != null) {
        copyAttributeIfPresent(m3customJndiNameResolver.getChildren().stream()
            .filter(p -> "jndiInitialFactory".equals(p.getAttributeValue("key"))).findFirst().get(), nameResolverBuilder, "value",
                               "jndiInitialContextFactory");
        copyAttributeIfPresent(m3customJndiNameResolver.getChildren().stream()
            .filter(p -> "jndiProviderUrl".equals(p.getAttributeValue("key"))).findFirst().get(), nameResolverBuilder, "value",
                               "jndiProviderUrl");

        m3customJndiNameResolver.getChildren("property", CORE_NAMESPACE)
            .forEach(prop -> {
              if ("jndiProviderProperties".equals(prop.getAttributeValue("key"))) {
                processProviderPropertiesRef(prop.getAttributeValue("value-ref"), appModel, nameResolverBuilder);
              }
            });
      }

      if ("true".equals(m3Connector.getAttributeValue("jndiDestinations"))) {
        if ("true".equals(m3Connector.getAttributeValue("forceJndiDestinations"))) {
          jndiConnFactory.setAttribute("lookupDestination", "ALWAYS");
        } else {
          jndiConnFactory.setAttribute("lookupDestination", "TRY_ALWAYS");
        }
      }


      jndiConnFactory.addContent(nameResolverBuilder);

      Element connFactory = new Element("connection-factory", JMS_NAMESPACE).addContent(jndiConnFactory);

      connection.addContent(connFactory);
    }
  }

  private static void processProviderProperties(final Element m3Connector, ApplicationModel appModel,
                                                Element nameResolverBuilder) {
    processProviderPropertiesRef(m3Connector.getAttributeValue("jndiProviderProperties-ref"), appModel, nameResolverBuilder);
  }

  private static void processProviderPropertiesRef(String jndiProviderPropertiesRef, ApplicationModel appModel,
                                                   Element nameResolverBuilder) {
    if (jndiProviderPropertiesRef != null) {
      Element providerProperties = new Element("provider-properties", JMS_NAMESPACE);
      nameResolverBuilder.addContent(providerProperties);

      appModel.getNodes("//*[@id='" + jndiProviderPropertiesRef + "']/spring:prop").forEach(prop -> {
        providerProperties.addContent(new Element("provider-property", JMS_NAMESPACE)
            .setAttribute("key", prop.getAttributeValue("key"))
            .setAttribute("value", prop.getTextTrim()));
      });
    }
  }

  private static Element addActiveMqConnection(final Element m4JmsConfig, final Element m3Connector) {
    Element amqConnection = new Element("active-mq-connection", JMS_NAMESPACE);
    m4JmsConfig.addContent(amqConnection);

    boolean addFactory = false;
    Element factoryConfiguration = new Element("factory-configuration", JMS_NAMESPACE);

    if (m3Connector.getAttribute("brokerURL") != null) {
      factoryConfiguration.setAttribute("brokerUrl", m3Connector.getAttributeValue("brokerURL"));
      addFactory = true;
    }


    if (m3Connector.getAttributeValue("maxRedelivery") != null) {
      factoryConfiguration.setAttribute("maxRedelivery", m3Connector.getAttributeValue("maxRedelivery"));
      addFactory = true;
    }

    if (addFactory) {
      amqConnection.addContent(factoryConfiguration);
    }

    // TODO:
    // disableTemporaryDestinations -> JmsMessageDispatcher.469
    // jms/integration/activemq-config.xml
    return amqConnection;
  }
}
