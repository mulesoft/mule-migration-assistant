/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NS_SCHEMA_LOC;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Provides reusable methods for common migration scenarios.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public final class XmlDslUtils {

  public static final String CORE_NS_URI = "http://www.mulesoft.org/schema/mule/core";
  public static final Namespace CORE_NAMESPACE = Namespace.getNamespace(CORE_NS_URI);

  public static final String EE_NAMESPACE_NAME = "ee";
  public static final String CORE_EE_NS_URI = "http://www.mulesoft.org/schema/mule/ee/core";
  public static final Namespace CORE_EE_NAMESPACE = Namespace.getNamespace(EE_NAMESPACE_NAME, CORE_EE_NS_URI);
  public static final String EE_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd";

  private XmlDslUtils() {
    // Nothing to do
  }

  /**
   * Sets the given {@code text} on the given {@code element}, wrapping it in a {@code CDATA} for readability if it contains
   * special characters.
   *
   * @param element
   * @param text
   * @return
   */
  public static Element setText(Element element, String text) {
    if (text.contains("<") || text.contains(">") || text.contains("&") || text.contains("'") || text.contains("\"")) {
      element.setContent(new CDATA(text));
    } else {
      element.setText(text);
    }

    return element;
  }

  /**
   * Assuming the value of {@code attr} is an expression, migrate it and update the value.
   *
   * @param attr the attribute containing the expression to migrate
   * @param exprMigrator the migrator for the expressions
   */
  public static void migrateExpression(Attribute attr, ExpressionMigrator exprMigrator) {
    if (attr != null) {
      attr.setValue(exprMigrator.migrateExpression(attr.getValue(), true, attr.getParent()));
    }
  }

  /**
   * Migrate a field for which the default value was changed.
   *
   * @param oldDefaultValue
   * @param newDefaultValue
   * @param currentValue
   * @return the value to set in the new version, or null if {@code currentValue} is already the new default.
   */
  public static String changeDefault(String oldDefaultValue, String newDefaultValue, String currentValue) {
    if (currentValue == null) {
      return oldDefaultValue;
    } else if (newDefaultValue.equals(currentValue)) {
      return null;
    } else {
      return currentValue;
    }
  }

  /**
   * Add the required compatibility elements to the flow for a migrated source to work correctly.
   */
  public static void migrateSourceStructure(ApplicationModel appModel, Element object, MigrationReport report) {
    migrateSourceStructure(appModel, object, report, true, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated source to work correctly.
   */
  public static void migrateSourceStructure(ApplicationModel appModel, Element object, MigrationReport report,
                                            boolean expectsOutboundProperties, boolean consumeStreams) {
    addCompatibilityNamespace(object.getDocument());

    int index = object.getParent().indexOf(object);
    buildAttributesToInboundProperties(report, object.getParent(), index + 1);

    if (expectsOutboundProperties) {
      Element errorHandlerElement = getFlowExceptionHandlingElement(object.getParentElement());
      if (errorHandlerElement != null) {
        buildOutboundPropertiesToVar(report, object.getParent(), object.getParentElement().indexOf(errorHandlerElement) - 1,
                                     consumeStreams);

        errorHandlerElement.getChildren()
            .forEach(eh -> buildOutboundPropertiesToVar(report, eh, eh.getContentSize(), consumeStreams));
      } else {
        buildOutboundPropertiesToVar(report, object.getParent(), object.getParent().getContentSize(), consumeStreams);
      }
    }
  }

  /**
   * Add the required compatibility elements to the flow for a migrated operation to work correctly.
   */
  public static void migrateOperationStructure(ApplicationModel appModel, Element object, MigrationReport report) {
    migrateOperationStructure(appModel, object, report, true, null, null, false);
  }

  public static void migrateOperationStructure(ApplicationModel appModel, Element object, MigrationReport report,
                                               boolean outputsAttributes, ExpressionMigrator expressionMigrator,
                                               CompatibilityResolver resolver) {
    migrateOperationStructure(appModel, object, report, outputsAttributes, expressionMigrator, resolver, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated operation to work correctly.
   */
  public static void migrateOperationStructure(ApplicationModel appModel, Element object, MigrationReport report,
                                               boolean outputsAttributes, ExpressionMigrator expressionMigrator,
                                               CompatibilityResolver resolver, boolean consumeStreams) {
    if (expressionMigrator != null && resolver != null) {
      migrateEnrichers(object, expressionMigrator, resolver, appModel, report);
    }
    addCompatibilityNamespace(object.getDocument());

    int index = object.getParent().indexOf(object);

    if (!"true".equals(object.getAttributeValue("isPolledConsumer", Namespace.getNamespace("migration", "migration")))) {
      buildOutboundPropertiesToVar(report, object.getParent(), index, consumeStreams);
    }
    if (outputsAttributes) {
      buildAttributesToInboundProperties(report, object.getParent(), index + 2);
    }
  }

  public static void migrateEnrichers(Element object, ExpressionMigrator expressionMigrator,
                                      CompatibilityResolver<String> resolver, ApplicationModel model,
                                      MigrationReport report) {
    String targetValue = object.getAttributeValue("target");
    if (isNotBlank(targetValue)) {
      String migratedExpression = expressionMigrator.migrateExpression(targetValue, true, object, true);
      object.setAttribute("target", expressionMigrator.unwrap(migratedExpression));
      if (resolver.canResolve(expressionMigrator.unwrap(targetValue))) {
        addOutboundPropertySetter(expressionMigrator.unwrap(migratedExpression), object, model, object);
        report.report("message.outboundPropertyEnricher", object, object);
      }
    }
  }

  public static Element addOutboundPropertySetter(String propertyName, Element element, ApplicationModel model,
                                                  Element after) {
    addCompatibilityNamespace(element.getDocument());
    Element setProperty = new Element("set-property", COMPATIBILITY_NAMESPACE);
    setProperty.setAttribute(new Attribute("propertyName", propertyName));
    setProperty.setAttribute(new Attribute("value", "#[vars." + propertyName + "]"));

    addElementAfter(setProperty, after);
    return setProperty;
  }

  private static Element buildAttributesToInboundProperties(MigrationReport report, Parent parent, int index) {
    Element a2ip = new Element("attributes-to-inbound-properties", COMPATIBILITY_NAMESPACE);
    parent.addContent(index, a2ip);

    report.report("message.attributesToInboundProperties", a2ip, a2ip);
    return a2ip;
  }

  private static Element buildOutboundPropertiesToVar(MigrationReport report, Parent parent, int index, boolean consumeStreams) {
    Element op2v = new Element("outbound-properties-to-var", COMPATIBILITY_NAMESPACE);

    if (consumeStreams) {
      op2v.setAttribute("consumeStreams", "true");
    }

    parent.addContent(index, op2v);

    report.report("message.outboundProperties", op2v, op2v);

    return op2v;
  }

  public static void migrateRedeliveryPolicyChildren(Element redeliveryPolicy, MigrationReport report) {
    Element dlq = redeliveryPolicy.getChild("dead-letter-queue", CORE_NAMESPACE);
    if (dlq != null) {
      Element flow = getFlow(redeliveryPolicy);
      Element errorHandler = getFlowExceptionHandlingElement(flow);

      if (errorHandler == null) {
        errorHandler = new Element("error-handler", CORE_NAMESPACE);
        flow.addContent(errorHandler);
      }

      Optional<Element> redeliveryExhaustedHandler =
          errorHandler.getChildren().stream().filter(e -> "REDELIVERY_EXHAUSTED".equals(e.getAttributeValue("type"))).findFirst();

      if (redeliveryExhaustedHandler.isPresent()) {
        report.report("flow.redeliveryExhausted", dlq, redeliveryPolicy);
      } else {
        Element handler = new Element("on-error-propagate", CORE_NAMESPACE).setAttribute("type", "REDELIVERY_EXHAUSTED");
        errorHandler.addContent(0, handler);
        handler.addContent(dlq.getChildren().stream().map(c -> c.detach()).collect(toList()));
      }

      dlq.detach();
    }

  }

  /**
   * Add the required compatibility namespace declaration on document.
   */
  public static void addCompatibilityNamespace(Document document) {
    addNameSpace(COMPATIBILITY_NAMESPACE, COMPATIBILITY_NS_SCHEMA_LOC, document);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param attributeName the name of the attribute to move from source to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String attributeName) {
    return copyAttributeIfPresent(source, target, attributeName, attributeName, attrValue -> attrValue, true);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param sourceAttributeName the name of the attribute to remove from source
   * @param targetAttributeName the name of the attribute to add to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                               final String targetAttributeName) {
    return copyAttributeIfPresent(source, target, sourceAttributeName, targetAttributeName, attrValue -> attrValue, true);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param sourceAttributeName the name of the attribute to remove from source
   * @param targetAttributeName the name of the attribute to add to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                               final String targetAttributeName, boolean removeSource) {
    return copyAttributeIfPresent(source, target, sourceAttributeName, targetAttributeName, attrValue -> attrValue, removeSource);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param sourceAttributeName the name of the attribute to remove from source
   * @param targetAttributeName the name of the attribute to add to target
   * @param attributeValueMapper functional interface to map attribute values
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                               final String targetAttributeName,
                                               AttributeValueMapper<String> attributeValueMapper) {
    return copyAttributeIfPresent(source, target, sourceAttributeName, targetAttributeName, attributeValueMapper, true);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param attributeName the name of the attribute to move from source to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String attributeName,
                                               boolean removeSource) {
    return copyAttributeIfPresent(source, target, attributeName, attributeName, attrValue -> attrValue, removeSource);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param attributeName the name of the attribute to move from source to target
   * @param attributeValueMapper functional interface to map attribute values
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String attributeName,
                                               AttributeValueMapper<String> attributeValueMapper,
                                               boolean removeSource) {
    return copyAttributeIfPresent(source, target, attributeName, attributeName, attributeValueMapper, removeSource);
  }

  /**
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param sourceAttributeName the name of the attribute to remove from source
   * @param targetAttributeName the name of the attribute to add to target
   * @param attributeValueMapper functional interface to map attribute values
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                               final String targetAttributeName,
                                               AttributeValueMapper<String> attributeValueMapper, boolean removeSource) {
    if (hasAttribute(source, sourceAttributeName)) {
      target.setAttribute(targetAttributeName,
                          attributeValueMapper.getAttributeValue(source.getAttributeValue(sourceAttributeName)));
      if (removeSource) {
        source.removeAttribute(sourceAttributeName);
      }
      return true;
    } else {
      return false;
    }
  }

  public static boolean hasAttribute(final Element source, final String sourceAttributeName) {
    return source.getAttribute(sourceAttributeName) != null;
  }

  /**
   * Add new element before some existing element.
   *
   * @param newElement
   * @param element
   */
  public static void addElementBefore(Element newElement, Element element) {
    Integer elementIndex = element.getParentElement().indexOf(element);
    element.getParentElement().addContent(elementIndex, newElement);
  }

  /**
   * Add new element after some existing element.
   *
   * @param newElement
   * @param element
   */
  public static void addElementAfter(Element newElement, Element element) {
    Integer elementIndex = element.getParentElement().indexOf(element);
    element.getParentElement().addContent(elementIndex + 1, newElement);
  }

  /**
   * Add new element after some existing element.
   *
   * @param newElements
   * @param element
   */
  public static void addElementsAfter(Collection<? extends Content> newElements, Element element) {
    Integer elementIndex = element.getParentElement().indexOf(element);
    element.getParentElement().addContent(elementIndex + 1, newElements);
  }

  public static Element getFlow(Element processor) {
    while (processor != null && !"flow".equals(processor.getName()) && !"sub-flow".equals(processor.getName())) {
      processor = processor.getParentElement();
    }

    return processor;
  }

  public static boolean isTopLevelElement(Element element) {
    return (element.getParentElement().equals(element.getDocument().getRootElement()));
  }

  public static void createErrorHandlerParent(Element element) {
    Element parent = element.getParentElement();
    element.detach();

    Element errorHandler = new Element("error-handler");
    errorHandler.setNamespace(CORE_NAMESPACE);
    errorHandler.addContent(element);

    if (element.getAttribute("name") != null) {
      Attribute name = element.getAttribute("name");
      name.detach();
      errorHandler.setAttribute(name);
    }

    parent.addContent(errorHandler);
  }

  public static void removeAttribute(Element element, String attributeName) {
    if (hasAttribute(element, attributeName)) {
      element.removeAttribute(attributeName);
    }
  }

  public static void removeAllAttributes(Element element) {
    List<Attribute> attributes = element.getAttributes().stream().collect(toList());
    attributes.forEach(Attribute::detach);
  }

  public static void addMigrationAttributeToElement(Element element, Attribute attribute) {
    attribute.setNamespace(Namespace.getNamespace("migration", "migration"));
    element.setAttribute(attribute);
  }

  public static boolean isErrorHanldingElement(Element element) {
    return element.getName()
        .matches("choice-exception-strategy|catch-exception-strategy|rollback-exception-strategy|exception-strategy|error-handler");
  }

  public static Element getFlowExceptionHandlingElement(Element flow) {
    return flow.getChildren().stream().filter(e -> isErrorHanldingElement(e)).findFirst().orElse(null);
  }

  /**
   * Add element at the end of the flow before the exception handling components.
   *
   * @param flow
   * @param newElement
   */
  public static void addElementToBottom(Element flow, Element newElement) {
    Element exceptionHandling = getFlowExceptionHandlingElement(flow);
    Integer newElementIndex = exceptionHandling != null ? flow.indexOf(exceptionHandling) : flow.getContentSize();
    flow.addContent(newElementIndex, newElement);
  }

  /**
   * Add new top level element after all the existing ones.
   *
   * @param element
   * @param document
   */
  public static void addTopLevelElement(Element element, Document document) {
    Integer elementIndex = document.getRootElement().getContent().indexOf(document.getRootElement().getChildren().stream()
        .filter(c -> c.getName().matches("flow|sub-flow")).findFirst().orElse(null));
    if (elementIndex >= 0) {
      document.getRootElement().addContent(elementIndex, element);
    } else {
      elementIndex = document.getRootElement().getContent().size();
      document.getRootElement().addContent(elementIndex > 0 ? elementIndex - 1 : 0, element);
    }
  }

  public static void migrateReconnection(Element m4Connection, Element m3Connector, MigrationReport report) {
    String failsDeployment = changeDefault("true", "false", m3Connector.getAttributeValue("validateConnections"));
    m3Connector.removeAttribute("validateConnections");

    Element reconnection = new Element("reconnection", CORE_NAMESPACE);
    if (failsDeployment != null) {
      reconnection.setAttribute("failsDeployment", failsDeployment);
      m4Connection.addContent(reconnection);
    }

    migrateReconnect(m3Connector, reconnection, report, m4Connection);
  }

  /**
   *
   * @param m3Connection the source or connector config that may contain a {@code reconnect element}.
   */
  public static void migrateReconnect(Element m3Connection, Element m4Reconnection, MigrationReport report, Element parent) {
    Element reconnectForever = m3Connection.getChild("reconnect-forever", CORE_NAMESPACE);
    if (reconnectForever != null) {
      m4Reconnection.addContent(reconnectForever.detach());
    }

    Element reconnect = m3Connection.getChild("reconnect", CORE_NAMESPACE);
    if (reconnect != null) {
      final Element m4Reconnect = reconnect.detach();

      if (m4Reconnect.getChild("reconnect-notifier", CORE_NAMESPACE) != null
          || m4Reconnect.getChild("reconnect-custom-notifier", CORE_NAMESPACE) != null) {
        report.report("connectors.reconnectNotifiers", reconnect, parent);

        m4Reconnect.removeChildren("reconnect-notifier", CORE_NAMESPACE);
        m4Reconnect.removeChildren("reconnect-custom-notifier", CORE_NAMESPACE);
      }

      if (m4Reconnect.hasAttributes() || !m4Reconnect.getChildren().isEmpty()) {
        m4Reconnection.addContent(m4Reconnect);
      }
    }
  }

  /**
   * Create a new flow element with the given name and add it after the {@param previousSibling}
   * @param name name for the flow to create
   * @param previousSibling an element that will be before the flow in the file
   * @return the new flow element
   */
  public static Element addNewFlowAfter(String name, Element previousSibling) {
    Element flow = new Element("flow", CORE_NAMESPACE);
    flow.setAttribute("name", name);
    addElementAfter(flow, previousSibling);
    return flow;
  }

  /**
   * Return JDOM document from a file path.
   *
   * @param filePath
   * @return the jdom document.
   */
  public static Document generateDocument(Path filePath) throws JDOMException, IOException {
    SAXBuilder saxBuilder = new SAXBuilder();
    saxBuilder.setJDOMFactory(new LocatedJDOMFactory());
    return saxBuilder.build(filePath.toFile());
  }

  public static boolean isMuleConfigFile(String fileName, Path appBasePath) {
    boolean muleConfig = false;
    if (fileName.endsWith("xml")) {
      File xmlFile = FileUtils.listFiles(appBasePath.toFile(), new String[] {"xml"}, true).stream()
          .filter(f -> f.getName().equals(fileName.replace("classpath:", ""))).findFirst().orElse(null);
      if (xmlFile != null) {
        try {
          Document doc = generateDocument(xmlFile.toPath());
          if (doc.getRootElement().getNamespace().getURI().startsWith("http://www.mulesoft.org/schema/mule/")) {
            muleConfig = true;
          }
        } catch (Exception ex) {
        }
      }
    }
    return muleConfig;
  }

  public static String getXPathSelector(String namespaceUri, String elementName) {
    return getXPathSelector(namespaceUri, elementName, false);
  }

  public static String getTopLevelXPathSelector(String namespaceUri, String elementName) {
    return getXPathSelector(namespaceUri, elementName, true);
  }

  public static String getXPathSelector(String namespaceUri, String elementName, boolean topLevel) {
    return format("%s[namespace-uri() = '%s' and local-name() = '%s']", topLevel ? "/*/*" : "//*", namespaceUri, elementName);
  }

  public static String getCoreXPathSelector(String elementName) {
    return getCoreXPathSelector(elementName, false);
  }

  public static String getTopLevelCoreXPathSelector(String elementName) {
    return getCoreXPathSelector(elementName, true);
  }

  private static String getCoreXPathSelector(String elementName, boolean topLevel) {
    return getXPathSelector(CORE_NS_URI, elementName, topLevel);
  }
}
