/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.mulesoft.tools.migration.library.mule.steps.amqp.AmqpConnector.XPATH_SELECTOR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.hasAttribute;
import static java.lang.Boolean.parseBoolean;
import static java.util.Optional.of;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.library.mule.steps.amqp.values.AckModeAttributeMapper;
import com.mulesoft.tools.migration.library.mule.steps.amqp.values.DeliveryModeAttributeMapper;
import com.mulesoft.tools.migration.library.mule.steps.amqp.values.SimpleAttributeMapper;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Migrates the endpoints of the AMQP Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractAmqpEndpoint extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  protected static final String AMQP_NAMESPACE_PREFIX = "amqp";
  protected static final String AMQPS_NAMESPACE_PREFIX = "amqps";
  public static final String AMQP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/amqp";
  public static final String AMQPS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/amqps";
  public static final Namespace AMQP_NAMESPACE = Namespace.getNamespace(AMQP_NAMESPACE_PREFIX, AMQP_NAMESPACE_URI);
  public static final Namespace AMQPS_NAMESPACE = Namespace.getNamespace(AMQPS_NAMESPACE_PREFIX, AMQPS_NAMESPACE_URI);
  public static final Namespace TLS_NAMESPACE = getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

  private static final Map<String, SimpleAttributeMapper> CONNECTION_ATTRIBUTES;

  private static final Map<String, SimpleAttributeMapper> CONSUMER_CONFIG_ATTRIBUTES;

  private static final Map<String, SimpleAttributeMapper> PUBLISHER_CONFIG_ATTRIBUTES;

  private static final Map<String, SimpleAttributeMapper> QOS_CONFIG_ATTRIBUTES;

  private ExpressionMigrator expressionMigrator;

  static {
    CONNECTION_ATTRIBUTES = new HashMap<String, SimpleAttributeMapper>();
    CONNECTION_ATTRIBUTES.put("username", new SimpleAttributeMapper("username"));
    CONNECTION_ATTRIBUTES.put("password", new SimpleAttributeMapper("password"));
    CONNECTION_ATTRIBUTES.put("host", new SimpleAttributeMapper("host"));
    CONNECTION_ATTRIBUTES.put("port", new SimpleAttributeMapper("port"));
    CONNECTION_ATTRIBUTES.put("virtualHost", new SimpleAttributeMapper("virtualHost"));
    CONNECTION_ATTRIBUTES.put("requestedHeartbeat", new SimpleAttributeMapper("heartbeatTimeout"));

    CONSUMER_CONFIG_ATTRIBUTES = new HashMap<String, SimpleAttributeMapper>();
    CONSUMER_CONFIG_ATTRIBUTES.put("ackMode", new AckModeAttributeMapper());
    CONSUMER_CONFIG_ATTRIBUTES.put("numberOfChannels", new SimpleAttributeMapper("numberOfConsumers"));
    CONSUMER_CONFIG_ATTRIBUTES.put("noLocal", new SimpleAttributeMapper("noLocal"));
    CONSUMER_CONFIG_ATTRIBUTES.put("exclusiveConsumers", new SimpleAttributeMapper("exclusiveConsumers"));

    PUBLISHER_CONFIG_ATTRIBUTES = new HashMap<String, SimpleAttributeMapper>();
    PUBLISHER_CONFIG_ATTRIBUTES.put("deliveryMode", new DeliveryModeAttributeMapper());
    PUBLISHER_CONFIG_ATTRIBUTES.put("priority", new SimpleAttributeMapper("priority"));
    PUBLISHER_CONFIG_ATTRIBUTES.put("mandatory", new SimpleAttributeMapper("mandatory"));
    PUBLISHER_CONFIG_ATTRIBUTES.put("immediate", new SimpleAttributeMapper("immediate"));
    PUBLISHER_CONFIG_ATTRIBUTES.put("requestBrokerConfirms", new SimpleAttributeMapper("requestBrokerConfirms"));

    QOS_CONFIG_ATTRIBUTES = new HashMap<String, SimpleAttributeMapper>();
    QOS_CONFIG_ATTRIBUTES.put("prefetchSize", new SimpleAttributeMapper("prefetchSize"));
    QOS_CONFIG_ATTRIBUTES.put("prefetchCount", new SimpleAttributeMapper("prefetchCount"));
  }

  protected static Optional<Element> resolveAmqpConnector(Element object, ApplicationModel appModel) {
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

  public static String migrateAmqpConfig(Element object, MigrationReport report, Optional<Element> connector,
                                         ApplicationModel appModel) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "AmqpConfig");

    Optional<Element> config = appModel.getNodeOptional("*/*[(namespace-uri()='" + AMQP_NAMESPACE_URI
        + "' or namespace-uri()='" + AMQPS_NAMESPACE_URI + "') and local-name()='config' and @name='" + configName + "']");

    config.orElseGet(() -> {
      final Element amqpConfig = new Element("config", AMQP_NAMESPACE);
      amqpConfig.setAttribute("name", configName);

      connector.ifPresent(conn -> {
        addConnectionToConfig(amqpConfig, conn, appModel, report);

        if (hasAttribute(conn, "fallbackAddresses")) {
          report.report("amqp.fallbackAddresses", conn, amqpConfig);
        }

        if (hasAttribute(conn, "default-return-endpoint-ref")) {
          report.report("amqp.returnListener", conn, amqpConfig);
        }

        if (mustAddConsumerConfig(conn)) {
          addConsumerConfigToConfig(amqpConfig, conn, appModel, report);
        }

        if (mustAddPublisherConfig(conn)) {
          addPublisherConfigToConfig(amqpConfig, conn, appModel, report);
        }

        if (mustAddQoSConfig(conn)) {
          addQoSConfigToConfig(amqpConfig, conn, appModel, report);
        }

        if (hasAttribute(conn, "activeDeclarationsOnly")) {
          String fallbackQueueActionValue =
              Boolean.toString(!parseBoolean(conn.getAttribute("activeDeclarationsOnly").getValue()));
          amqpConfig.setAttribute("createFallbackQueue", fallbackQueueActionValue);
          amqpConfig.setAttribute("createFallbackExchange", fallbackQueueActionValue);
          report.report("activeDeclarationsOnly", conn, amqpConfig);
        }
      });

      addTopLevelElement(amqpConfig, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return amqpConfig;
    });
    return configName;
  }

  private static void addQoSConfigToConfig(Element amqpConfig, Element m3Connector, ApplicationModel appModel,
                                           MigrationReport report) {
    addAdditionalConfig(amqpConfig, m3Connector, appModel, report, "quality-of-service-config", QOS_CONFIG_ATTRIBUTES);

  }

  private static void addPublisherConfigToConfig(Element amqpConfig, Element m3Connector, ApplicationModel appModel,
                                                 MigrationReport report) {
    addAdditionalConfig(amqpConfig, m3Connector, appModel, report, "publisher-config", PUBLISHER_CONFIG_ATTRIBUTES);
  }

  private static boolean mustAddQoSConfig(Element connector) {
    return mustAddAdditionalConfig(connector, QOS_CONFIG_ATTRIBUTES.keySet());
  }

  private static boolean mustAddPublisherConfig(Element connector) {
    return mustAddAdditionalConfig(connector, PUBLISHER_CONFIG_ATTRIBUTES.keySet());
  }

  private static boolean mustAddConsumerConfig(Element connector) {
    return mustAddAdditionalConfig(connector, CONSUMER_CONFIG_ATTRIBUTES.keySet());
  }

  private static boolean mustAddAdditionalConfig(Element connector, Set<String> attributes) {
    for (String attribute : attributes) {
      if (hasAttribute(connector, attribute)) {
        return true;
      }
    }

    return false;
  }

  private static void addConsumerConfigToConfig(Element amqpConfig, Element m3Connector, ApplicationModel appModel,
                                                MigrationReport report) {
    addAdditionalConfig(amqpConfig, m3Connector, appModel, report, "consumer-config", CONSUMER_CONFIG_ATTRIBUTES);

  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

  public static Element addAdditionalConfig(final Element m4AmqpConfig, final Element m3Connector, ApplicationModel appModel,
                                            MigrationReport report, String additionalConfigName,
                                            Map<String, SimpleAttributeMapper> attributesMap) {

    Element connection = new Element(additionalConfigName, AMQP_NAMESPACE);
    m4AmqpConfig.addContent(connection);

    attributesMap.keySet()
        .forEach(attr -> copyAttributeIfPresent(m3Connector, connection, attr, attributesMap.get(attr).getAttributeName(),
                                                attributesMap.get(attr)));

    return connection;
  }

  public static void addConnectionToConfig(final Element m4AmqpConfig, final Element m3Connector, ApplicationModel appModel,
                                           MigrationReport report) {
    Element conn = addAdditionalConfig(m4AmqpConfig, m3Connector, appModel, report, "connection", CONNECTION_ATTRIBUTES);

    if (!hasAttribute(conn, "host")) {
      conn.setAttribute("host", "localhost");
    }

    if (!hasAttribute(conn, "username")) {
      conn.setAttribute("username", "guest");
    }

    if (!hasAttribute(conn, "password")) {
      conn.setAttribute("password", "guest");
    }

    if (m3Connector.getNamespace().equals(AMQPS_NAMESPACE)) {
      setTlsContext(m3Connector, conn);
    }
  }

  private static void setTlsContext(Element amqp3Connector, Element conn) {
    conn.setAttribute("useTls", "true");
    List<Element> inputChildren = new ArrayList<>(amqp3Connector.getChildren());
    Element tlsContext = new Element("context", TLS_NAMESPACE);
    copyAttributeIfPresent(amqp3Connector, tlsContext, "sslProtocol", "enabledProtocols");
    inputChildren.forEach(child -> {
      if (child.getNamespace().equals(AMQPS_NAMESPACE) && child.getName().equals("ssl-key-store")) {
        Element keyStore = new Element("key-store", TLS_NAMESPACE);
        copyAttributeIfPresent(child, keyStore, "path");
        copyAttributeIfPresent(child, keyStore, "storePassword", "password");
        copyAttributeIfPresent(child, keyStore, "keyPassword");
        copyAttributeIfPresent(child, keyStore, "type");
        copyAttributeIfPresent(child, keyStore, "alias");
        copyAttributeIfPresent(child, keyStore, "algorithm");
        tlsContext.addContent(keyStore);
      }

      if (child.getNamespace().equals(AMQPS_NAMESPACE) && child.getName().equals("ssl-trust-store")) {
        Element trustStore = new Element("trust-store", TLS_NAMESPACE);
        copyAttributeIfPresent(child, trustStore, "path");
        copyAttributeIfPresent(child, trustStore, "storePassword", "password");
        copyAttributeIfPresent(child, trustStore, "type", "type");
        tlsContext.addContent(trustStore);
      }
    });

    if (tlsContext.hasAttributes() || !tlsContext.getChildren().isEmpty()) {
      conn.addContent(tlsContext);
    }

  }

  protected String resolveRemovalStrategy(Boolean autoDelete, Boolean durable) {
    if (!durable && !autoDelete) {
      return "SHUTDOWN";
    }

    if (durable && !autoDelete) {
      return "EXPLICIT";
    }

    if (!durable && autoDelete) {
      return "UNUSED";
    }

    return null;
  }

}
