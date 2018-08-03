/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.mulesoft.tools.migration.library.mule.steps.core.properties.InboundPropertiesHelper.addAttributesMapping;
import static com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector.XPATH_SELECTOR;

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
    expressionsPerProperty.put("JMSPriority", "message.attributes.headers.priority");

    try {
      addAttributesMapping(appModel, "org.mule.extensions.jms.api.message.JmsAttributes", expressionsPerProperty);
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
