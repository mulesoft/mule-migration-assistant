/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;

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
      + "local-name() = 'activemq-xa-connector')]";

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

  public static void addConnectionToConfig(final Element m4JmsConfig, final Element m3Connector) {
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
      default:
        connection = new Element("generic-connection", JMS_NAMESPACE);
        m4JmsConfig.addContent(connection);
    }

    String m4Specification = changeDefault("1.0.2b", "1.1", m3Connector.getAttributeValue("specification"));
    if (m4Specification != null && m4Specification.equals("1.0.2b")) {
      connection.setAttribute("specification", "JMS_1_0_2b");
    }

  }

  private static Element addActiveMqConnection(final Element m4JmsConfig, final Element m3Connector) {
    Element amqConnection = new Element("active-mq-connection", JMS_NAMESPACE);
    m4JmsConfig.addContent(amqConnection);

    if (m3Connector.getAttributeValue("maxRedelivery") != null) {
      amqConnection.addContent(new Element("factory-configuration", JMS_NAMESPACE)
          .setAttribute("maxRedelivery", m3Connector.getAttributeValue("maxRedelivery")));
    }
    // TODO:
    // disableTemporaryDestinations -> JmsMessageDispatcher.469
    // jms/integration/activemq-config.xml
    return amqConnection;
  }
}
