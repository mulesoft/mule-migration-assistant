/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.getMigrationScriptFolder;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.library;
import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector.XPATH_SELECTOR;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.addSharedLibs;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.lang.System.lineSeparator;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Migrates the endpoints of the JMS Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractJmsEndpoint extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  protected static final String JMS_NAMESPACE_PREFIX = "jms";
  protected static final String JMS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/jms";
  public static final Namespace JMS_NAMESPACE = Namespace.getNamespace(JMS_NAMESPACE_PREFIX, JMS_NAMESPACE_URI);

  private ExpressionMigrator expressionMigrator;

  public static void addAttributesToInboundProperties(Element object, ApplicationModel appModel, MigrationReport report) {
    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("JMSCorrelationID", "message.attributes.headers.correlationId");
    expressionsPerProperty.put("MULE_CORRELATION_ID", "message.attributes.headers.correlationId");
    expressionsPerProperty.put("JMSDeliveryMode", "message.attributes.headers.deliveryMode");
    expressionsPerProperty.put("JMSDestination", "message.attributes.headers.destination");
    expressionsPerProperty.put("JMSExpiration", "message.attributes.headers.expiration");
    expressionsPerProperty.put("JMSMessageID", "message.attributes.headers.messageId");
    expressionsPerProperty.put("JMSPriority", "message.attributes.headers.priority");
    expressionsPerProperty.put("JMSRedelivered", "message.attributes.headers.redelivered");
    expressionsPerProperty.put("JMSReplyTo", "message.attributes.headers.replyTo.destination");
    expressionsPerProperty.put("JMSTimestamp", "message.attributes.headers.timestamp");
    expressionsPerProperty.put("JMSType", "message.attributes.headers['type']");

    try {
      addAttributesMapping(appModel, "org.mule.extensions.jms.api.message.JmsAttributes", expressionsPerProperty,
                           "message.attributes.properties.userProperties");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Element compatibilityProperties(ApplicationModel appModel) {
    return new Element("properties", JMS_NAMESPACE)
        .setText("#[migration::JmsTransport::jmsPublishProperties(vars)]");
  }

  public static void jmsTransportLib(ApplicationModel appModel) {
    try {
      // Replicates logic from org.mule.transport.jms.transformers.AbstractJmsTransformer.setJmsProperties(MuleMessage, Message)
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "JmsTransport.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the properties building logic of the Mule 3.x JMS Connector." + lineSeparator() +
                  " * Replicates logic from org.mule.transport.jms.transformers.AbstractJmsTransformer.setJmsProperties(MuleMessage, Message)."
                  + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun jmsPublishProperties(vars: {}) = do {" + lineSeparator() +
                  "    var jmsProperties = ['JMSCorrelationID', 'JMSDeliveryMode', 'JMSDestination', 'JMSExpiration',"
                  + " 'JMSMessageID', 'JMSPriority', 'JMSRedelivered', 'JMSReplyTo', 'JMSTimestamp', 'JMSType',"
                  + " 'selector', 'MULE_REPLYTO']" + lineSeparator() +
                  "    ---" + lineSeparator() +
                  "    vars.compatibility_outboundProperties default {} filterObject" + lineSeparator() +
                  "    ((value,key) -> not contains(jmsProperties, (key as String)))" + lineSeparator() +
                  "    mapObject ((value, key, index) -> {" + lineSeparator() +
                  "        ((key as String) replace \" \" with \"_\") : value" + lineSeparator() +
                  "        })" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Adapts the Mule 4 correlationId to the way it was used in 3.x" + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun jmsCorrelationId(correlationId, vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_outboundProperties.MULE_CORRELATION_ID default correlationId" + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Adapts the Mule 4 correlationId to the way it was used in 3.x" + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun jmsSendCorrelationId(vars: {}) = do {" + lineSeparator() +
                  "    if (vars.compatibility_outboundProperties.MULE_CORRELATION_ID == null) 'NEVER' else 'ALWAYS'"
                  + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "/**" + lineSeparator() +
                  " * Adapts the Mule 4 reply-to to the way it was used in 3.x" + lineSeparator() +
                  " */" + lineSeparator() +
                  "fun jmsPublishReplyTo(vars: {}) = do {" + lineSeparator() +
                  "    vars.compatibility_inboundProperties.JMSReplyTo default" + lineSeparator() +
                  "    (if (vars.compatibility_outboundProperties.MULE_REPLYTO != null)" + lineSeparator() +
                  "        (vars.compatibility_outboundProperties.MULE_REPLYTO splitBy 'jms://')[1]" + lineSeparator() +
                  "        else null)"
                  + lineSeparator() +
                  "}" + lineSeparator() +
                  "" + lineSeparator() +
                  "" + lineSeparator() +
                  "" + lineSeparator() +
                  lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Optional<Element> resolveJmsConnector(Element object, ApplicationModel appModel) {
    Optional<Element> connector;
    if (object.getAttribute("connector-ref") != null) {
      connector = of(getConnector(object.getAttributeValue("connector-ref"), appModel));
      object.removeAttribute("connector-ref");
    } else {
      connector = getDefaultConnector(appModel);
    }
    return connector;
  }

  protected static Element getConnector(String connectorName, ApplicationModel appModel) {
    return appModel.getNode(StringUtils.substring(XPATH_SELECTOR, 0, -1) + " and @name = '" + connectorName + "']");
  }

  protected static Optional<Element> getDefaultConnector(ApplicationModel appModel) {
    return appModel.getNodeOptional(XPATH_SELECTOR);
  }

  public static String migrateJmsConfig(Element object, MigrationReport report, Optional<Element> connector,
                                        ApplicationModel appModel) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "JmsConfig");

    Optional<Element> config = appModel.getNodeOptional("*/jms:config[@name='" + configName + "']");
    Element jmsConfig = config.orElseGet(() -> {
      final Element jmsCfg = new Element("config", JMS_NAMESPACE);
      jmsCfg.setAttribute("name", configName);

      connector.ifPresent(conn -> {
        addConnectionToConfig(jmsCfg, conn, appModel, report);
      });

      addTopLevelElement(jmsCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return jmsCfg;
    });
    return configName;
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  public static void addConnectionToConfig(final Element m4JmsConfig, final Element m3Connector, ApplicationModel appModel,
                                           MigrationReport report) {
    Element connection;
    switch (m3Connector.getName()) {
      case "activemq-connector":
        connection = addActiveMqConnection(m4JmsConfig, m3Connector, appModel);
        break;
      case "activemq-xa-connector":
        connection = addActiveMqConnection(m4JmsConfig, m3Connector, appModel);

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
      case "weblogic-connector":
        report.report(ERROR, m3Connector, m4JmsConfig, "Add the client library of the Weblogic MQ as a shared library.",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#using-a-different-broker");

        connection = new Element("generic-connection", JMS_NAMESPACE);
        m4JmsConfig.addContent(connection);
        break;
      case "websphere-connector":
        // TODO MMT-202
        report.report(ERROR, m3Connector, m4JmsConfig, "IBM MQ Connector should be used to connect to an IBM MQ broker.",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-jms#using-a-different-broker");

        connection = new Element("generic-connection", JMS_NAMESPACE);
        m4JmsConfig.addContent(connection);
        break;
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
          appModel.getNode("/*/*[@name='" + m3Connector.getAttributeValue("connectionFactory-ref") + "']");
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

  private static Element addActiveMqConnection(final Element m4JmsConfig, final Element m3Connector, ApplicationModel appModel) {
    Dependency activeMqClient = new DependencyBuilder()
        .withGroupId("org.apache.activemq")
        .withArtifactId("activemq-client")
        .withVersion("${activeMq.version}")
        .build();

    addSharedLibs(appModel.getPomModel().get(), activeMqClient);

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

    return amqConnection;
  }
}
