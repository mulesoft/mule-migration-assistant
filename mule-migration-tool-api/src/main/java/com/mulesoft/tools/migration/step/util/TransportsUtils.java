/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getFlowExceptionHandlingElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isErrorHanldingElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateSourceStructure;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides reusable methods for common transports migration scenarios.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public final class TransportsUtils {

  private static final String COMPATIBILITY_NS_URI = "http://www.mulesoft.org/schema/mule/compatibility";
  protected static final String COMPATIBILITY_NS_SCHEMA_LOC =
      "http://www.mulesoft.org/schema/mule/compatibility/current/mule-compatibility.xsd";

  public static final Namespace COMPATIBILITY_NAMESPACE = getNamespace("compatibility", COMPATIBILITY_NS_URI);

  // Cannot use just Uri() because the address may have placeholders
  public static final Pattern ADDRESS_PATTERN =
      compile("(\\w+):\\/\\/(?:(.*)@)?([^\\/:]*)(?::([^\\/]+))?(\\/.*)?");

  private TransportsUtils() {
    // Nothing to do
  }

  public static Optional<EndpointAddress> processAddress(Element endpoint, MigrationReport report) {
    if (endpoint.getAttribute("address") == null) {
      return empty();
    }

    String address = endpoint.getAttributeValue("address");
    Matcher addressMatcher = ADDRESS_PATTERN.matcher(address);

    if (addressMatcher.matches()) {
      String protocol = addressMatcher.group(1);
      String credentials = addressMatcher.group(2);
      String host = addressMatcher.group(3);
      String port = addressMatcher.group(4);
      String path = addressMatcher.group(5);

      endpoint.removeAttribute("address");

      if ("file".equals(protocol) || "jms".equals(protocol) || "vm".equals(protocol)) {
        // Ref: https://stackoverflow.com/questions/7857416/file-uri-scheme-and-relative-files
        return of(new EndpointAddress(protocol, credentials, null, port, host + (path != null ? path : "")));
      } else {
        return of(new EndpointAddress(protocol, credentials, host, port, path));
      }
    } else {
      report.report("transports.cantParseAddress", endpoint, endpoint, address);
      return empty();
    }
  }

  /**
   * Represents a uri address
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static final class EndpointAddress {

    private final String protocol;
    private final String credentials;
    private final String host;
    private final String port;
    private final String path;

    public EndpointAddress(String protocol, String credentials, String host, String port, String path) {
      this.protocol = protocol;
      this.credentials = credentials;
      this.host = host;
      this.port = port;
      this.path = path;
    }

    public String getProtocol() {
      return protocol;
    }

    public String getCredentials() {
      return credentials;
    }

    public String getHost() {
      return host;
    }

    public String getPort() {
      return port;
    }

    public String getPath() {
      return path;
    }
  }

  public static void handleReconnection(Element mule3Connector, Element connection) {
    String failsDeployment = changeDefault("true", "false", mule3Connector.getAttributeValue("validateConnections"));
    mule3Connector.removeAttribute("validateConnections");
    if (failsDeployment != null) {
      Element reconnection = new Element("reconnection", CORE_NAMESPACE);
      reconnection.setAttribute("failsDeployment", failsDeployment);
      connection.addContent(reconnection);
    }

    if (mule3Connector.getChild("reconnect", CORE_NAMESPACE) != null) {
      Element reconnection = connection.getChild("reconnection", CORE_NAMESPACE);
      if (reconnection == null) {
        reconnection = new Element("reconnection", CORE_NAMESPACE);
        connection.addContent(reconnection);
      }

      reconnection.addContent(mule3Connector.getChild("reconnect", CORE_NAMESPACE).detach());
    }

    if (mule3Connector.getChild("reconnect-forever", CORE_NAMESPACE) != null) {
      Element reconnection = connection.getChild("reconnection", CORE_NAMESPACE);
      if (reconnection == null) {
        reconnection = new Element("reconnection", CORE_NAMESPACE);
        connection.addContent(reconnection);
      }

      reconnection.addContent(mule3Connector.getChild("reconnect-forever", CORE_NAMESPACE).detach());
    }
  }

  public static void handleConnectorChildElements(Element object, Element m4Config, Element connection, MigrationReport report) {
    Element receiverThreadingProfile = object.getChild("receiver-threading-profile", CORE_NAMESPACE);
    if (receiverThreadingProfile != null) {
      report.report("flow.threading", receiverThreadingProfile, m4Config);
      object.removeContent(receiverThreadingProfile);
    }

    Element dispatcherThreadingProfile = object.getChild("dispatcher-threading-profile", CORE_NAMESPACE);
    if (dispatcherThreadingProfile != null) {
      report.report("flow.threading", receiverThreadingProfile, m4Config);
      object.removeContent(dispatcherThreadingProfile);
    }

    Element reconnect = object.getChild("reconnect", CORE_NAMESPACE);

    if (reconnect != null) {
      Element reconnectNotification = reconnect.getChild("reconnect-custom-notifier", CORE_NAMESPACE);
      if (reconnectNotification != null) {
        report.report("transports.reconnectCustomNotifier", dispatcherThreadingProfile, m4Config);
        reconnectNotification.detach();
      }

      reconnect.removeAttribute("blocking");

      Element reconnection = connection.getChild("reconnection", CORE_NAMESPACE);
      if (reconnection == null) {
        reconnection = new Element("reconnection", CORE_NAMESPACE);
        connection.addContent(reconnection);
      }

      copyAttributeIfPresent(reconnect, reconnection, "frequency");
      copyAttributeIfPresent(reconnect, reconnection, "count");

      if (reconnect.getAttributes().size() == 0 && reconnect.getChildren().isEmpty()) {
        reconnect.detach();
      }
    }
  }

  /**
   * Add the required compatibility elements to the flow for a migrated inbound endpoint to work correctly.
   */
  public static void migrateInboundEndpointStructure(ApplicationModel appModel, Element inboundEndpoint, MigrationReport report,
                                                     boolean expectsOutboundProperties) {
    migrateInboundEndpointStructure(appModel, inboundEndpoint, report, expectsOutboundProperties, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated inbound endpoint to work correctly.
   */
  public static void migrateInboundEndpointStructure(ApplicationModel appModel, Element inboundEndpoint, MigrationReport report,
                                                     boolean expectsOutboundProperties, boolean consumeStreams) {
    inboundEndpoint.removeAttribute("exchange-pattern");
    inboundEndpoint.removeAttribute("disableTransportTransformer");
    extractInboundChildren(inboundEndpoint, appModel);
    migrateSourceStructure(appModel, inboundEndpoint, report, expectsOutboundProperties, consumeStreams);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated outbound endpoint to work correctly.
   */
  public static void migrateOutboundEndpointStructure(ApplicationModel appModel, Element outboundEndpoint, MigrationReport report,
                                                      boolean outputsAttributes) {
    migrateOutboundEndpointStructure(appModel, outboundEndpoint, report, outputsAttributes, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated outbound endpoint to work correctly.
   */
  public static void migrateOutboundEndpointStructure(ApplicationModel appModel, Element outboundEndpoint, MigrationReport report,
                                                      boolean outputsAttributes, boolean consumeStreams) {
    if (outboundEndpoint.getAttributeValue("exchange-pattern") != null
        && "one-way".equals(outboundEndpoint.getAttributeValue("exchange-pattern"))) {
      Element asyncWrapper = new Element("async", CORE_NAMESPACE);
      Element nestedAsync = asyncWrapper;

      // may be a try scope too
      Element flow = outboundEndpoint.getParentElement();
      Element errorHandler = getFlowExceptionHandlingElement(flow);

      if (errorHandler != null) {
        nestedAsync = new Element("try", CORE_NAMESPACE);
        asyncWrapper.addContent(nestedAsync);
      }

      List<Element> allChildren = flow.getChildren();
      for (Element processor : new ArrayList<>(allChildren.subList(allChildren.indexOf(outboundEndpoint), allChildren.size()))) {
        if (!isErrorHanldingElement(processor)) {
          nestedAsync.addContent(processor.detach());
        } else {
          nestedAsync.addContent(processor.clone());
        }
      }

      if (errorHandler != null) {
        flow.addContent(flow.indexOf(errorHandler), asyncWrapper);
      } else {
        flow.addContent(asyncWrapper);
      }
    }
    outboundEndpoint.removeAttribute("exchange-pattern");
    outboundEndpoint.removeAttribute("disableTransportTransformer");
    extractOutboundChildren(outboundEndpoint, appModel);
    migrateOperationStructure(appModel, outboundEndpoint, report, outputsAttributes, null, null, consumeStreams);
  }

  public static void extractInboundChildren(Element inbound, ApplicationModel appModel) {
    extractInboundChildren(inbound, 2, inbound.getParentElement(), appModel);
  }

  public static void extractInboundChildren(Element inbound, final int index,
                                            final Element target, ApplicationModel appModel) {
    target.addContent(index, fetchContent(inbound, appModel));

    // may be a try scope too
    Element flow = target;
    Element errorHandler = getFlowExceptionHandlingElement(flow);
    if (errorHandler != null) {
      flow.addContent(flow.indexOf(errorHandler),
                      fetchResponseContent(inbound, appModel));
    } else {
      flow.addContent(fetchResponseContent(inbound, appModel));
    }
  }

  public static void extractOutboundChildren(Element outbound, ApplicationModel appModel) {
    outbound.getParentElement().addContent(outbound.getParentElement().indexOf(outbound), fetchContent(outbound, appModel));
    outbound.getParentElement().addContent(outbound.getParentElement().indexOf(outbound) + 1,
                                           fetchResponseContent(outbound, appModel));
  }

  private static List<Content> fetchContent(Element endpoint, ApplicationModel appModel) {
    List<Content> content = new ArrayList<>();
    if (endpoint.getChild("properties", CORE_NAMESPACE) != null) {
      for (Element element : endpoint.getChild("properties", CORE_NAMESPACE).getChildren()) {
        if ("entry".equals(element.getName())
            && "http://www.springframework.org/schema/beans".equals(element.getNamespace().getURI())) {
          content.add(new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", element.getAttributeValue("key"))
              .setAttribute("value", element.getAttributeValue("value")));
        }
      }
      endpoint.getChild("properties", CORE_NAMESPACE).detach();
    }

    if (endpoint.getAttribute("transformer-refs") != null) {
      String[] transformerNames = endpoint.getAttributeValue("transformer-refs").split(",");

      for (String transformerName : transformerNames) {
        Element transformer = appModel.getNode("/*/*[@name = '" + transformerName + "']");
        if ("message-properties-transformer".equals(transformer.getName())) {
          Element clonedMpt = transformer.clone();
          clonedMpt.removeAttribute("name");
          content.add(clonedMpt);
        } else {
          content.add(new Element("transformer", CORE_NAMESPACE).setAttribute("ref", transformerName));
        }
      }
      endpoint.removeAttribute("transformer-refs");
    }

    endpoint.getChildren().stream()
        .filter(c -> c.getName().contains("transformer") || c.getName().contains("filter") || "set-property".equals(c.getName())
            || "processor".equals(c.getName()) || "logger".equals(c.getName()))
        .collect(toList()).forEach(tc -> {
          tc.getParent().removeContent(tc);
          content.add(tc);
        });
    return content;
  }

  private static List<Content> fetchResponseContent(Element endpoint, ApplicationModel appModel) {
    List<Content> responseContent = new ArrayList<>();
    if (endpoint.getChild("response", CORE_NAMESPACE) != null) {
      responseContent.addAll(endpoint.getChild("response", CORE_NAMESPACE).cloneContent());
      endpoint.getChild("response", CORE_NAMESPACE).detach();
    }

    if (endpoint.getAttribute("responseTransformer-refs") != null) {
      String[] transformerNames = endpoint.getAttributeValue("responseTransformer-refs").split(",");

      for (String transformerName : transformerNames) {
        Element transformer = appModel.getNode("/*/*[@name = '" + transformerName + "']");
        if ("message-properties-transformer".equals(transformer.getName())) {
          Element clonedMpt = transformer.clone();
          clonedMpt.removeAttribute("name");
          responseContent.add(clonedMpt);
        } else {
          responseContent.add(new Element("transformer", CORE_NAMESPACE).setAttribute("ref", transformerName));
        }
      }
      endpoint.removeAttribute("responseTransformer-refs");
    }
    return responseContent;
  }

  /**
   * The endpoint may already have a {@code scheduling-strategy} because it was migrated from a Quartz transport. If not, the
   * default values are set.
   *
   * @param endpoint
   */
  public static void migrateSchedulingStrategy(Element endpoint, OptionalInt defaultFreq) {
    Element schedulingStr = endpoint.getChild("scheduling-strategy", CORE_NAMESPACE);
    if (schedulingStr == null) {
      schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
      schedulingStr.addContent(new Element("fixed-frequency", CORE_NAMESPACE));
      endpoint.addContent(schedulingStr);
    }

    Element fixedFrequency = schedulingStr.getChild("fixed-frequency", CORE_NAMESPACE);
    if (fixedFrequency != null) {
      if (endpoint.getAttribute("pollingFrequency") != null) {
        fixedFrequency.setAttribute("frequency", endpoint.getAttributeValue("pollingFrequency", "1000"));
      } else if (fixedFrequency.getAttribute("frequency") == null) {
        defaultFreq.ifPresent(df -> fixedFrequency.setAttribute("frequency", Integer.toString(df)));
      }
      endpoint.removeAttribute("pollingFrequency");
    }
  }
}
