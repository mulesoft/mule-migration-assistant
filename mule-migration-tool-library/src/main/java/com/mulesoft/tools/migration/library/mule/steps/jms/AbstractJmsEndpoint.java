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
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
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
  protected static final Namespace JMS_NAMESPACE = Namespace.getNamespace(JMS_NAMESPACE_PREFIX, JMS_NAMESPACE_URI);

  private ExpressionMigrator expressionMigrator;

  // protected String obtainPath(Element object) {
  // String path = object.getAttributeValue("path");
  //
  // if (path.contains("?")) {
  // String[] splitPath = path.split("\\?");
  // path = splitPath[0];
  //
  // for (String urlParam : splitPath[1].split("&")) {
  // String[] splitUrlParam = urlParam.split("=");
  //
  // String key = splitUrlParam[0];
  // String value = splitUrlParam[1];
  //
  // if ("responseTransformers".equals(key)) {
  // object.setAttribute("responseTransformer-refs", value);
  // } else {
  // object.setAttribute(key, value);
  // }
  // }
  // }
  // return path;
  // }

  // protected void addQueue(final Namespace vmConnectorNamespace, Optional<Element> connector, Element vmConfig, String path) {
  // Element queues = vmConfig.getChild("queues", vmConnectorNamespace);
  // if (!queues.getChildren().stream().filter(e -> path.equals(e.getAttributeValue("queueName"))).findAny().isPresent()) {
  // Element queue = new Element("queue", vmConnectorNamespace);
  // queue.setAttribute("queueName", path);
  //
  // queue.setAttribute("queueType", "TRANSIENT");
  // connector.ifPresent(conn -> {
  // Optional<Element> queueProfile = empty();
  // if (conn.getChild("queueProfile", VM_NAMESPACE) != null) {
  // queueProfile = of(conn.getChild("queueProfile", VM_NAMESPACE));
  // } else if (conn.getChild("queue-profile", VM_NAMESPACE) != null) {
  // queueProfile = of(conn.getChild("queue-profile", VM_NAMESPACE));
  // }
  //
  // if ("true".equals(queueProfile.map(qp -> qp.getAttributeValue("persistent")).orElse(null))) {
  // queue.setAttribute("queueType", "PERSISTENT");
  // }
  // if (queueProfile.map(qp -> qp.getAttribute("maxOutstandingMessages")).orElse(null) != null) {
  // queue.setAttribute("maxOutstandingMessages", queueProfile.get().getAttributeValue("maxOutstandingMessages"));
  // }
  // });
  // queues.addContent(queue);
  // }
  // }
  //
  // protected Element buildContent(final Namespace vmConnectorNamespace) {
  // // TODO MMT-166 Use something that includes the extra parameters in the media type, instead of ^mimeType
  // // (https://github.com/mulesoft/data-weave/issues/296)
  // return new Element("content", vmConnectorNamespace)
  // .setText("#[output application/java --- {'_vmTransportMode': true, 'payload': payload.^raw, 'mimeType': payload.^mimeType,
  // 'session': vars.compatibility_outboundProperties['MULE_SESSION']}]");
  // }

  public static void addAttributesToInboundProperties(Element object, ApplicationModel appModel, MigrationReport report) {
    Map<String, String> expressionsPerProperty = new LinkedHashMap<>();
    expressionsPerProperty.put("JMSCorrelationID", "message.attributes.headers.correlationId");
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
      // Replicates logic fom org.mule.transport.jms.transformers.AbstractJmsTransformer.setJmsProperties(MuleMessage, Message)
      library(getMigrationScriptFolder(appModel.getProjectBasePath()), "JmsTransport.dwl",
              "" +
                  "/**" + lineSeparator() +
                  " * Emulates the properties building logic of the Mule 3.x JMS Connector." + lineSeparator() +
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
                  "fun jmsCorrelationId(vars: {}) = do {" + lineSeparator() +
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

  protected Element getConnector(String connectorName) {
    return getApplicationModel()
        .getNode(StringUtils.substring(XPATH_SELECTOR, 0, -1) + " and @name = '" + connectorName + "']");
  }

  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel().getNodeOptional(XPATH_SELECTOR);
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
