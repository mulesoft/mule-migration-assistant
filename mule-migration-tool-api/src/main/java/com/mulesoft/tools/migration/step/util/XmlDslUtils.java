/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.project.model.ApplicationModel.addNameSpace;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NS_SCHEMA_LOC;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jdom2.Namespace.getNamespace;

import com.google.common.collect.Maps;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Parent;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.xpath.XPathFactory;

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
  public static final Namespace MIGRATION_NAMESPACE = Namespace.getNamespace("migration", "migration");
  public static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";
  public static final Namespace HTTP_NAMESPACE = getNamespace("http", HTTP_NAMESPACE_URI);

  public static final String MIGRATION_ID_ATTRIBUTE = "migrationId";

  private XmlDslUtils() {
    // Nothing to do
  }

  /**
   * Sets the given {@code text} on the given {@code element}, wrapping it in a {@code CDATA} for readability if it contains
   * special characters.
   *
   * @param element the element where to add text
   * @param text the text to add on the element
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
   * @param oldDefaultValue the old value
   * @param newDefaultValue the new value
   * @param currentValue the actual value on the element
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
   *
   * @param appModel the application model representation
   * @param element the source element to migrate using compatibility module
   * @param report the migration report
   */
  public static void migrateSourceStructureForCompatibility(ApplicationModel appModel, Element element, MigrationReport report) {
    migrateSourceStructureForCompatibility(appModel, element, report, true, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated source to work correctly.
   *
   * @param appModel the application model representation
   * @param element the source element to migrate using compatibility module
   * @param report the migration report
   * @param expectsOutboundProperties should it declare outbound properties
   * @param consumeStreams should properties be declared as streams
   */
  public static void migrateSourceStructureForCompatibility(ApplicationModel appModel, Element element, MigrationReport report,
                                                            boolean expectsOutboundProperties, boolean consumeStreams) {
    addCompatibilityNamespace(element.getDocument());
    int index = element.getParent().indexOf(element);
    buildAttributesToInboundProperties(report, element.getParent(), index + 1);
    if (expectsOutboundProperties) {
      Element errorHandlerElement = getFlowExceptionHandlingElement(element.getParentElement());
      if (errorHandlerElement != null) {
        buildOutboundPropertiesToVar(report, element.getParent(), element.getParentElement().indexOf(errorHandlerElement) - 1,
                                     consumeStreams);

        errorHandlerElement.getChildren()
            .forEach(eh -> buildOutboundPropertiesToVar(report, eh, eh.getContentSize(), consumeStreams));
      } else {
        buildOutboundPropertiesToVar(report, element.getParent(), element.getParent().getContentSize(), consumeStreams);
      }
    }
  }

  /**
   * Add the required compatibility elements to the flow for a migrated operation to work correctly.
   *
   * @param appModel the application model representation
   * @param element the processor to migrate using compatibility module
   * @param report the migration report
   */
  public static void migrateOperationStructure(ApplicationModel appModel, Element element, MigrationReport report) {
    migrateOperationStructure(appModel, element, report, true, null, null, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated operation to work correctly.
   *
   * @param appModel the application model representation
   * @param element the processor to migrate using compatibility module
   * @param report the migration report
   * @param outputsAttributes should it declare attributes
   * @param expressionMigrator the expressions migration engine
   * @param resolver the compatibility module resolver
   */
  public static void migrateOperationStructure(ApplicationModel appModel, Element element, MigrationReport report,
                                               boolean outputsAttributes, ExpressionMigrator expressionMigrator,
                                               CompatibilityResolver resolver) {
    migrateOperationStructure(appModel, element, report, outputsAttributes, expressionMigrator, resolver, false);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated operation to work correctly.
   *
   * @param appModel the application model representation
   * @param element the processor to migrate using compatibility module
   * @param report the migration report
   * @param outputsAttributes should it declare attributes
   * @param expressionMigrator the expressions migration engine
   * @param resolver the compatibility module resolver
   * @param consumeStreams  should properties be declared as streams
   */
  public static void migrateOperationStructure(ApplicationModel appModel, Element element, MigrationReport report,
                                               boolean outputsAttributes, ExpressionMigrator expressionMigrator,
                                               CompatibilityResolver resolver, boolean consumeStreams) {
    if (expressionMigrator != null && resolver != null) {
      migrateEnrichers(element, expressionMigrator, resolver, appModel, report);
    }

    if (!appModel.noCompatibilityMode()) {
      addCompatibilityNamespace(element.getDocument());
      int index = element.getParent().indexOf(element);
      if (!"true".equals(element.getAttributeValue("isPolledConsumer", MIGRATION_NAMESPACE))) {
        buildOutboundPropertiesToVar(report, element.getParent(), index, consumeStreams);
      }
      if (outputsAttributes) {
        buildAttributesToInboundProperties(report, element.getParent(), index + 2);
      }
    } else {
      report.report("nocompatibility.notfullyimplemented", element, element);
    }
  }

  /**
   * Migrate enricher expressions
   *
   * @param element the enricher element
   * @param expressionMigrator he expressions migration engine
   * @param resolver the compatibility module resolver
   * @param appModel the application model representation
   * @param resolver the compatibility module resolver
   * @param report the migration report
   */
  public static void migrateEnrichers(Element element, ExpressionMigrator expressionMigrator,
                                      CompatibilityResolver<String> resolver, ApplicationModel appModel,
                                      MigrationReport report) {
    String targetValue = element.getAttributeValue("target");
    if (isNotBlank(targetValue)) {
      String migratedExpression = expressionMigrator.migrateExpression(targetValue, true, element, true);
      element.setAttribute("target", expressionMigrator.unwrap(migratedExpression));
      if (resolver.canResolve(expressionMigrator.unwrap(targetValue))) {
        addOutboundPropertySetter(expressionMigrator.unwrap(migratedExpression), element, appModel, element);
        report.report("message.outboundPropertyEnricher", element, element);
      }
    }
  }

  /**
   * Add the compatibility element to convert properties to outbound properties
   *
   * @param propertyName the name of the outbound property
   * @param element the current element that will use the outbound property
   * @param appModel the application model representation
   * @param after the element after the new set-property processor will be added
   */
  public static Element addOutboundPropertySetter(String propertyName, Element element, ApplicationModel appModel,
                                                  Element after) {
    addCompatibilityNamespace(element.getDocument());
    Element setProperty = new Element("set-property", COMPATIBILITY_NAMESPACE);
    setProperty.setAttribute(new Attribute("propertyName", propertyName));
    setProperty.setAttribute(new Attribute("value", "#[vars." + propertyName + "]"));

    addElementAfter(setProperty, after);
    return setProperty;
  }

  /**
   * Add the compatibility element to convert attributes to outbound properties
   *
   * @param report the migration report
   * @param parent the top level element on the configuration
   * @param index the position where to add the attributes-to-inbound-properties processor
   */
  private static Element buildAttributesToInboundProperties(MigrationReport report, Parent parent, int index) {
    Element a2ip = new Element("attributes-to-inbound-properties", COMPATIBILITY_NAMESPACE);
    parent.addContent(index, a2ip);

    report.report("message.attributesToInboundProperties", a2ip, a2ip);
    return a2ip;
  }

  /**
   * Add the compatibility element to convert outbound properties to vars
   *
   * @param report the migration report
   * @param parent the top level element on the configuration
   * @param index the position where to add the outbound-properties-to-var processor
   * @param consumeStreams should properties be declared as streams
   */
  private static Element buildOutboundPropertiesToVar(MigrationReport report, Parent parent, int index, boolean consumeStreams) {
    Element op2v = new Element("outbound-properties-to-var", COMPATIBILITY_NAMESPACE);

    if (consumeStreams) {
      op2v.setAttribute("consumeStreams", "true");
    }

    parent.addContent(index, op2v);

    report.report("message.outboundProperties", op2v, op2v);

    return op2v;
  }

  /**
   * Migrate redelivery policy of Mule 3 into the new REDELIVERY_EXHAUSTED method on Mule 4
   *
   * @param redeliveryPolicy the Mule 3 redelivery policy element
   * @param report the migration report
   */
  public static void migrateRedeliveryPolicyChildren(Element redeliveryPolicy, MigrationReport report) {
    Element dlq = redeliveryPolicy.getChild("dead-letter-queue", CORE_NAMESPACE);
    if (dlq != null) {
      Element flow = getContainerElement(redeliveryPolicy);
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
   *
   * @param document the mule configuration file
   */
  public static void addCompatibilityNamespace(Document document) {
    addNameSpace(COMPATIBILITY_NAMESPACE, COMPATIBILITY_NS_SCHEMA_LOC, document);
  }

  /**
   * Move an existing attribute to a different element
   *
   * @param source the element to remove the attribute from
   * @param target the element to add the element to
   * @param attributeName the name of the attribute to move from source to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String attributeName) {
    return copyAttributeIfPresent(source, target, attributeName, attributeName, attrValue -> attrValue, true);
  }

  /**
   * Move an existing attribute to a different element
   *
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
   * Move an existing attribute to a different element
   *
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
   * Move an existing attribute to a different element
   *
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
   * Move an existing attribute to a different element
   *
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
   * Move an existing attribute to a different element
   *
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
   * Move an existing attribute to a different element
   *
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

  /**
   * Check that the element contains an attribute
   *
   * @param source the element
   * @param sourceAttributeName the attribute name
   * @return {@link Boolean}
   */
  public static boolean hasAttribute(final Element source, final String sourceAttributeName) {
    return source.getAttribute(sourceAttributeName) != null;
  }

  /**
   * Add new element before some existing element.
   *
   * @param newElement the new element to be added
   * @param element the element before the new element will be added
   */
  public static void addElementBefore(Element newElement, Element element) {
    Integer elementIndex = element.getParentElement().indexOf(element);
    element.getParentElement().addContent(elementIndex, newElement);
  }

  /**
   * Add new element after some existing element.
   *
   * @param newElement the new element to be added
   * @param element the element after the new element will be added
   */
  public static void addElementAfter(Element newElement, Element element) {
    Integer elementIndex = element.getParentElement().indexOf(element);
    element.getParentElement().addContent(elementIndex + 1, newElement);
  }

  /**
   * Add new element after some existing element.
   *
   * @param newElements the new elements to be added
   * @param element the element after the new element will be added
   */
  public static void addElementsAfter(Collection<? extends Content> newElements, Element element) {
    Integer elementIndex = element.getParentElement().indexOf(element);
    element.getParentElement().addContent(elementIndex + 1, newElements);
  }

  /**
   * Get the top level element in the configuration file that contains the procesor
   *
   * @param processor the element which you need to get the top level element
   */
  public static Element getContainerElement(Element processor) {
    while (processor != null && !"flow".equals(processor.getName()) && !"sub-flow".equals(processor.getName())
        && !"before".equals(processor.getName()) && !"after".equals(processor.getName())) {
      processor = processor.getParentElement();
    }

    return processor;
  }

  /**
   * Check if the processor is a top level element in the configuration file
   *
   * @param element the element to check if it is a top level element
   */
  public static boolean isTopLevelElement(Element element) {
    return (element.getParentElement().equals(element.getDocument().getRootElement()));
  }

  /**
   * Create top level error handler section on configuration file
   *
   * @param element the element to check if it is an error handler top level element
   */
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

  /**
   * Remove an attribute from element
   *
   * @param element the element where the attribute will be removed
   * @param attributeName the attribute name to be removed
   */
  public static void removeAttribute(Element element, String attributeName) {
    if (hasAttribute(element, attributeName)) {
      element.removeAttribute(attributeName);
    }
  }

  /**
   * Remove all attributes from element
   *
   * @param element the element where all the attributes will be removed
   */
  public static void removeAllAttributes(Element element) {
    List<Attribute> attributes = element.getAttributes().stream().collect(toList());
    attributes.forEach(Attribute::detach);
  }

  /**
   * Remove all attributes from element on a given namespace
   *
   * @param element the element where all the attributes will be removed
   */
  public static void removeAllAttributes(Element element, Namespace namespace) {
    List<Attribute> attributes = element.getAttributes().stream()
        .filter(att -> att.getNamespace().equals(namespace))
        .collect(toList());
    attributes.forEach(Attribute::detach);
  }

  public static void removeAllAttributesRecursive(Element element, Namespace namespace) {
    removeAllAttributes(element, namespace);
    element.getChildren().stream().forEach(e -> removeAllAttributesRecursive(e, namespace));
  }

  /**
   * Add a new attribute to identify for particular post-migration actions
   *
   * @param element the element where this attribute will be added
   * @param attribute the attribute to be added
   */
  public static void addMigrationAttributeToElement(Element element, Attribute attribute) {
    attribute.setNamespace(MIGRATION_NAMESPACE);
    element.setAttribute(attribute);
  }

  /**
   * Check if the element is a mule 3 error handling element
   *
   * @param element the element to check if it is an error handler element
   */
  public static boolean isErrorHanldingElement(Element element) {
    return element.getName()
        .matches("choice-exception-strategy|catch-exception-strategy|rollback-exception-strategy|exception-strategy|error-handler");
  }

  /**
   * Get the exception handler element for the selected flow
   *
   * @param flow the element to check if it is a flow
   */
  public static Element getFlowExceptionHandlingElement(Element flow) {
    return flow.getChildren().stream().filter(e -> isErrorHanldingElement(e)).findFirst().orElse(null);
  }

  /**
   * Add element at the end of the flow before the exception handling components.
   *
   * @param flow the top level element that will contain this new element
   * @param newElement the new element to be added
   */
  public static void addElementToBottom(Element flow, Content newElement) {
    Element exceptionHandling = getFlowExceptionHandlingElement(flow);
    Integer newElementIndex = exceptionHandling != null ? flow.indexOf(exceptionHandling) : flow.getContentSize();
    flow.addContent(newElementIndex, newElement);
  }

  /**
   * Add new top level element after all the existing ones.
   *
   * @param element the new element to be added
   * @param document the document where it will be added
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

  /**
   * Migrate reconnection section of Mule 3 to new Mule 4 strategy
   *
   * @param m4Connection the migrated connection
   * @param m3Connector the connector that uses that connection
   * @param report the migration report
   */
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
   * Migrate reconnection section of Mule 3 to new Mule 4 strategy
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
   *
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
   * @param filePath the path of the file
   * @return the jdom document.
   */
  public static Document generateDocument(Path filePath, boolean generateElementIds) throws JDOMException, IOException {
    SAXBuilder saxBuilder = new SAXBuilder();
    if (generateElementIds) {
      saxBuilder.setJDOMFactory(new LocatedIdJDOMFactory());
    } else {
      saxBuilder.setJDOMFactory(new LocatedJDOMFactory());
    }
    return saxBuilder.build(filePath.toFile());
  }

  /**
   * Check if the file is a Mule configuration file
   *
   * @param fileName the file name
   * @param appBasePath the application path
   * @return
   */
  public static boolean isMuleConfigFile(String fileName, Path appBasePath) {
    boolean muleConfig = false;
    if (fileName.endsWith("xml")) {
      File xmlFile = FileUtils.listFiles(appBasePath.toFile(), new String[] {"xml"}, true).stream()
          .filter(f -> f.getName().equals(fileName.replace("classpath:", ""))).findFirst().orElse(null);
      if (xmlFile != null) {
        try {
          Document doc = generateDocument(xmlFile.toPath(), false);
          if (doc.getRootElement().getNamespace().getURI().startsWith("http://www.mulesoft.org/schema/mule/")) {
            muleConfig = true;
          }
        } catch (Exception ex) {
        }
      }
    }
    return muleConfig;
  }

  /**
   * Get Xpath expression to select elements on the configuration file
   *
   * @param namespaceUri the namespace URI
   * @param elementName the element name
   * @return a String with the expression
   */
  public static String getXPathSelector(String namespaceUri, String elementName) {
    return getXPathSelector(namespaceUri, elementName, false);
  }

  /**
   * Get Xpath expression to select top level elements on the configuration file
   *
   * @param namespaceUri the namespace URI
   * @param elementName the element name
   * @return a String with the expression
   */
  public static String getTopLevelXPathSelector(String namespaceUri, String elementName) {
    return getXPathSelector(namespaceUri, elementName, true);
  }

  /**
   * Get Xpath expression to select elements on the configuration file
   *
   * @param namespaceUri the namespace URI
   * @param elementName the element name
   * @param topLevel is a top level element
   * @return a String with the expression
   */
  public static String getXPathSelector(String namespaceUri, String elementName, boolean topLevel) {
    return format("%s[namespace-uri() = '%s' and local-name() = '%s']", topLevel ? "/*/*" : "//*", namespaceUri, elementName);
  }

  /**
   * Get Xpath expression to select all elements from a given namespace on the configuration file
   *
   * @param namespaceUri the namespace URI
   * @param topLevel is a top level element
   * @param firstGenerationOnly only return the first generation of children
   * @return a String with the expression
   */
  public static String getAllElementsFromNamespaceXpathSelector(String namespaceUri, List<String> elements, boolean topLevel,
                                                                boolean firstGenerationOnly) {
    Map<String, List<String>> namespacedElements = Maps.newHashMap();
    namespacedElements.put(namespaceUri, elements);
    return getAllElementsFromNamespaceXpathSelector(namespacedElements, topLevel, firstGenerationOnly);
  }

  /**
   * Get Xpath expression to select all elements from a given namespace on the configuration file
   *
   * @param namespacedElements map of namespaces and elements to search
   * @param topLevel is a top level element
   * @param firstGenerationOnly only return the first generation of children
   * @return a String with the expression
   */
  public static String getAllElementsFromNamespaceXpathSelector(Map<String, List<String>> namespacedElements, boolean topLevel,
                                                                boolean firstGenerationOnly) {

    String localNameExpression = namespacedElements.entrySet().stream()
        .map(e -> String.format("(namespace-uri()='%s' and (%s))", e.getKey(),
                                e.getValue().stream().map(element -> String.format("local-name()='%s'", element)).collect(
                                                                                                                          Collectors
                                                                                                                              .joining(" or "))))
        .collect(Collectors.joining(" or "));
    return format("%s[%s]", topLevel ? "/*/*" : firstGenerationOnly ? "*" : "//*",
                  localNameExpression);
  }

  /**
   * Get Xpath expression to select Mule core elements on the configuration file
   *
   * @param elementName the element name
   * @return a String with the expression
   */
  public static String getCoreXPathSelector(String elementName) {
    return getCoreXPathSelector(elementName, false);
  }

  /**
   * Get Xpath expression to select Mule core top level elements on the configuration file
   *
   * @param elementName the element name
   * @return a String with the expression
   */
  public static String getTopLevelCoreXPathSelector(String elementName) {
    return getCoreXPathSelector(elementName, true);
  }

  /**
   * Get Xpath expression to select Mule core elements on the configuration file
   *
   * @param elementName the element name
   * @param topLevel is a top level element
   * @return a String with the expression
   */
  private static String getCoreXPathSelector(String elementName, boolean topLevel) {
    return getXPathSelector(CORE_NS_URI, elementName, topLevel);
  }

  /**
   * Remove nested comments on element to avoid having the same entrance more than one time
   *
   * @param element the element to remove comments
   */
  public static void removeNestedComments(Element element) {
    Iterator<Content> contentIterator = element.getContent().iterator();
    while (contentIterator.hasNext()) {
      Content content = contentIterator.next();
      if (content instanceof Comment) {
        contentIterator.remove();
      }

      if (content instanceof Element) {
        Element contentElement = (Element) content;
        removeNestedComments(contentElement);
      }
    }
  }

  public static <T> List<T> getChildrenMatchingExpression(Element elementToEvaluate, String expression, Filter<T> filter) {
    return XPathFactory.instance().compile(expression, filter).evaluate(elementToEvaluate);
  }

}
