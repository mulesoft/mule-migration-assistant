/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.endpoint;

import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.addAttributesToInboundProperties;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.migrateJmsConfig;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.resolveJmsConnector;
import static com.mulesoft.tools.migration.library.mule.steps.jms.JmsOutboundEndpoint.migrateOutboundJmsEndpoint;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.VM_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.getVmConfigName;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.migrateVmConfig;
import static com.mulesoft.tools.migration.library.mule.steps.vm.AbstractVmEndpoint.resolveVmConector;
import static com.mulesoft.tools.migration.library.mule.steps.vm.VmOutboundEndpoint.migrateOutboundVmEndpoint;
import static com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep.copyAttributes;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.extractInboundChildren;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateOutboundEndpointStructure;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlow;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the request-reply construct
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RequestReply extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:request-reply";

  @Override
  public String getDescription() {
    return "Migrate request-reply.";
  }

  public RequestReply() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (object.getAttribute("storePrefix") != null) {
      report.report("transports.requestReplyStorePrefix", object, object);
      object.removeAttribute("storePrefix");
    }

    final Element request = object.getChildren().get(0);
    final Element reply = object.getChildren().get(1);

    if (RequestReplyMigrableConnector.JMS.equals(resolveEndpointConnector(request))
        && RequestReplyMigrableConnector.JMS.equals(resolveEndpointConnector(reply))) {
      handleGlobalEndpointsRefs(request, reply);

      final Optional<Element> requestConnector = resolveJmsConnector(request, getApplicationModel());
      final Optional<Element> replyConnector = resolveJmsConnector(reply, getApplicationModel());

      if (!requestConnector.equals(replyConnector)) {
        restoreConnectorRef(request, reply, requestConnector, replyConnector);

        String destination = processAddress(reply, report).map(address -> {
          String path = address.getPath();
          if ("topic".equals(path)) {
            return "TOPIC:" + address.getPort();
          } else {
            return path;
          }
        }).orElseGet(() -> {
          if (reply.getAttributeValue("queue") != null) {
            return reply.getAttributeValue("queue");
          } else {
            return "TOPIC:" + reply.getAttributeValue("topic");
          }
        });
        request.setAttribute("reply-to", destination, Namespace.getNamespace("migration", "migration"));

        migrateToReplyFlow(object, report, request, reply);
        return;
      }

      migrateJmsRequestReply(object, report, request, reply, requestConnector);
    } else if (RequestReplyMigrableConnector.VM.equals(resolveEndpointConnector(request))
        && RequestReplyMigrableConnector.VM.equals(resolveEndpointConnector(reply))) {

      handleGlobalEndpointsRefs(request, reply);

      final Optional<Element> requestConnector = resolveVmConector(request, getApplicationModel());
      final Optional<Element> replyConnector = resolveVmConector(reply, getApplicationModel());

      if (!requestConnector.equals(replyConnector)) {
        restoreConnectorRef(request, reply, requestConnector, replyConnector);
        migrateToReplyFlow(object, report, request, reply);
        return;
      }

      migrateVmRequestReply(object, report, request, reply, requestConnector);
    } else {
      migrateToReplyFlow(object, report, request, reply);
    }
  }

  /**
   * Put back the {@code connector-ref} attributes so the next tasks can process it.
   *
   * @param request
   * @param reply
   * @param requestConnector
   * @param replyConnector
   */
  protected void restoreConnectorRef(final Element request, final Element reply, final Optional<Element> requestConnector,
                                     final Optional<Element> replyConnector) {
    requestConnector.ifPresent(c -> request.setAttribute("connector-ref", c.getAttributeValue("name")));
    replyConnector.ifPresent(c -> reply.setAttribute("connector-ref", c.getAttributeValue("name")));
  }

  protected void handleGlobalEndpointsRefs(final Element request, final Element reply) {
    if (request.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/*/*[@name = '" + request.getAttributeValue("ref") + "']");
      copyAttributes(globalEndpoint, request);
      request.removeAttribute("ref");
    }
    if (reply.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/*/*[@name = '" + reply.getAttributeValue("ref") + "']");
      copyAttributes(globalEndpoint, reply);
      reply.removeAttribute("ref");
    }
  }

  protected void migrateJmsRequestReply(Element object, MigrationReport report, final Element request, final Element reply,
                                        final Optional<Element> requestConnector) {
    getApplicationModel().addNameSpace(JMS_NAMESPACE, "http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd",
                                       object.getDocument());
    request.setNamespace(JMS_NAMESPACE);
    request.setName("publish-consume");

    String jmsConfig = migrateJmsConfig(request, report, requestConnector, getApplicationModel());
    migrateOutboundJmsEndpoint(request, report, requestConnector, jmsConfig, getApplicationModel());

    request.detach();
    addElementAfter(request, object);
    object.detach();

    final Element replyTo = request.getChild("message", JMS_NAMESPACE).getChild("reply-to", JMS_NAMESPACE);
    String destination = processAddress(reply, report).map(address -> {
      String path = address.getPath();
      if ("topic".equals(path)) {
        replyTo.setAttribute("destinationType", "TOPIC");
        return address.getPort();
      } else {
        return path;
      }
    }).orElseGet(() -> {
      if (reply.getAttributeValue("queue") != null) {
        return reply.getAttributeValue("queue");
      } else {
        replyTo.setAttribute("destinationType", "TOPIC");
        return reply.getAttributeValue("topic");
      }
    });
    replyTo.setAttribute("destination", destination);

    if (object.getAttribute("timeout") != null) {
      request.addContent(new Element("consume-configuration", JMS_NAMESPACE)
          .setAttribute("maximumWait", object.getAttributeValue("timeout")));
    }

    migrateOutboundEndpointStructure(getApplicationModel(), request, report, true);
    extractInboundChildren(reply, request.getParentElement().indexOf(request) + 2, request.getParentElement(),
                           getApplicationModel());
    addAttributesToInboundProperties(request, getApplicationModel(), report);
  }

  protected void migrateVmRequestReply(Element object, MigrationReport report, final Element request, final Element reply,
                                       final Optional<Element> requestConnector) {
    getApplicationModel().addNameSpace(VM_NAMESPACE, "http://www.mulesoft.org/schema/mule/vm/current/mule-vm.xsd",
                                       object.getDocument());
    request.setNamespace(VM_NAMESPACE);
    request.setName("publish-consume");

    report.report("vm.requestReplyInbound", reply, request);

    final String configName = getVmConfigName(object, requestConnector);
    Element vmConfig = migrateVmConfig(object, requestConnector, configName, getApplicationModel());
    migrateOutboundVmEndpoint(request, report, requestConnector, configName, vmConfig);

    request.detach();
    addElementAfter(request, object);
    object.detach();

    if (object.getAttribute("timeout") != null) {
      request.setAttribute("timeout", object.getAttributeValue("timeout"));
      request.setAttribute("timeoutUnit", "MILLISECONDS");
      object.removeAttribute("timeout");
    }

    migrateOutboundEndpointStructure(getApplicationModel(), request, report, true, true);
    extractInboundChildren(reply, request.getParentElement().indexOf(request) + 2, request.getParentElement(),
                           getApplicationModel());

  }

  protected void migrateToReplyFlow(Element object, MigrationReport report, final Element request, final Element reply) {
    final Element flow = getFlow(object);
    final Element replyFlow = new Element("flow", CORE_NAMESPACE);
    replyFlow.setAttribute("name", flow.getName() + "_reply");

    addElementAfter(replyFlow, flow);

    report.report("transports.requestReplySplit", object, request);

    request.detach();
    addElementAfter(request, object);
    object.detach();

    reply.detach();
    replyFlow.addContent(reply);

    final Element rrParent = request.getParentElement();
    while (rrParent.getContentSize() > rrParent.indexOf(request) + 1) {
      final Content content = rrParent.getContent().get(rrParent.indexOf(request) + 1);
      content.detach();
      replyFlow.addContent(content);
    }
  }

  private RequestReplyMigrableConnector resolveEndpointConnector(Element endpoint) {
    if (VM_NAMESPACE.equals(endpoint.getNamespace())) {
      return RequestReplyMigrableConnector.VM;
    } else if (JMS_NAMESPACE.equals(endpoint.getNamespace())) {
      return RequestReplyMigrableConnector.JMS;
    }

    if (endpoint.getAttribute("address") != null) {
      final String address = endpoint.getAttributeValue("address");
      if (address.startsWith("vm://")) {
        return RequestReplyMigrableConnector.VM;
      } else if (address.startsWith("jms://")) {
        return RequestReplyMigrableConnector.JMS;
      }
    }

    if (endpoint.getAttribute("ref") != null) {
      Element globalEndpoint = getApplicationModel().getNode("/*/*[@name = '" + endpoint.getAttributeValue("ref") + "']");
      return resolveEndpointConnector(globalEndpoint);
    }

    return RequestReplyMigrableConnector.OTHER;
  }

  private static enum RequestReplyMigrableConnector {
    JMS, VM, OTHER
  }
}
