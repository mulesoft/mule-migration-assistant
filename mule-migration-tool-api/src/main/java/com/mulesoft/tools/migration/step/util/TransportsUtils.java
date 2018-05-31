/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateSourceStructure;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  public static final Namespace COMPATIBILITY_NAMESPACE = Namespace.getNamespace("compatibility", COMPATIBILITY_NS_URI);

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

      if ("file".equals(protocol)) {
        // Ref: https://stackoverflow.com/questions/7857416/file-uri-scheme-and-relative-files
        return of(new EndpointAddress(protocol, credentials, null, port, host + (path != null ? path : "")));
      } else {
        return of(new EndpointAddress(protocol, credentials, host, port, path));
      }
    } else {
      report.report(ERROR, endpoint, endpoint, "Unable to parse endpoint address '" + address + "'");
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

  /**
   * Add the required compatibility elements to the flow for a migrated inbound endpoint to work correctly.
   */
  public static void migrateInboundEndpointStructure(ApplicationModel appModel, Element inboundEndpoint, MigrationReport report,
                                                     boolean expectsOutboundProperties) {
    inboundEndpoint.removeAttribute("exchange-pattern");
    extractInboundChildren(inboundEndpoint, appModel);
    migrateSourceStructure(appModel, inboundEndpoint, report, expectsOutboundProperties);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated outbound endpoint to work correctly.
   */
  public static void migrateOutboundEndpointStructure(ApplicationModel appModel, Element outboundEndpoint, MigrationReport report,
                                                      boolean outputsAttributes) {
    if (outboundEndpoint.getAttributeValue("exchange-pattern") != null
        && "one-way".equals(outboundEndpoint.getAttributeValue("exchange-pattern"))) {
      Element asyncWrapper = new Element("async", CORE_NAMESPACE);

      Element flow = outboundEndpoint.getParentElement();
      List<Element> allChildren = flow.getChildren();
      for (Element processor : new ArrayList<>(allChildren.subList(allChildren.indexOf(outboundEndpoint), allChildren.size()))) {
        if (!"error-handler".equals(processor.getName())) {
          asyncWrapper.addContent(processor.detach());
        }
      }
      if (flow.getChild("error-handler", CORE_NAMESPACE) != null) {
        flow.addContent(flow.indexOf(flow.getChild("error-handler", CORE_NAMESPACE)), asyncWrapper);
      } else {
        flow.addContent(asyncWrapper);
      }
    }
    outboundEndpoint.removeAttribute("exchange-pattern");
    extractOutboundChildren(outboundEndpoint, appModel);
    migrateOperationStructure(appModel, outboundEndpoint, report, outputsAttributes);
  }

  public static void extractInboundChildren(Element inbound, ApplicationModel appModel) {
    inbound.getParentElement().addContent(2, fetchContent(inbound));
    inbound.getParentElement().addContent(fetchResponseContent(inbound, appModel));
  }

  public static void extractOutboundChildren(Element outbound, ApplicationModel appModel) {
    outbound.getParentElement().addContent(0, fetchContent(outbound));
    outbound.getParentElement().addContent(fetchResponseContent(outbound, appModel));
  }

  private static List<Content> fetchContent(Element outbound) {
    List<Content> content = new ArrayList<>();
    if (outbound.getChild("properties", CORE_NAMESPACE) != null) {
      for (Element element : outbound.getChild("properties", CORE_NAMESPACE).getChildren()) {
        if ("entry".equals(element.getName())
            && "http://www.springframework.org/schema/beans".equals(element.getNamespace().getURI())) {
          content.add(new Element("set-variable", CORE_NAMESPACE)
              .setAttribute("variableName", element.getAttributeValue("key"))
              .setAttribute("value", element.getAttributeValue("value")));
        }
      }
      outbound.getChild("properties", CORE_NAMESPACE).detach();
    }

    outbound.getChildren().stream()
        .filter(c -> c.getName().contains("transformer") || c.getName().contains("filter") || "set-property".equals(c.getName()))
        .collect(toList()).forEach(tc -> {
          tc.getParent().removeContent(tc);
          content.add(tc);
        });
    return content;
  }

  private static List<Content> fetchResponseContent(Element outbound, ApplicationModel appModel) {
    List<Content> responseContent = new ArrayList<>();
    if (outbound.getChild("response", CORE_NAMESPACE) != null) {
      responseContent.addAll(outbound.getChild("response", CORE_NAMESPACE).cloneContent());
      outbound.getChild("response", CORE_NAMESPACE).detach();
    }

    if (outbound.getAttribute("responseTransformer-refs") != null) {
      String[] transformerNames = outbound.getAttributeValue("responseTransformer-refs").split(",");

      for (String transformerName : transformerNames) {
        Element transformer = appModel.getNode("/mule:mule/*[@name = '" + transformerName + "']");
        if ("message-properties-transformer".equals(transformer.getName())) {
          Element clonedMpt = transformer.clone();
          clonedMpt.removeAttribute("name");
          responseContent.add(clonedMpt);
        } else {
          responseContent.add(new Element("transformer", CORE_NAMESPACE).setAttribute("ref", transformerName));
        }
      }
      outbound.removeAttribute("responseTransformer-refs");
    }
    return responseContent;
  }
}
