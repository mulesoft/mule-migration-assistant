/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NS_SCHEMA_LOC;
import static java.lang.System.lineSeparator;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Parent;

import java.util.Optional;

/**
 * Provides reusable methods for common migration scenarios.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public final class XmlDslUtils {

  private static final String CORE_NS_URI = "http://www.mulesoft.org/schema/mule/core";

  public static final Namespace CORE_NAMESPACE = Namespace.getNamespace(CORE_NS_URI);

  public static final Namespace VALIDATION_NAMESPACE =
      Namespace.getNamespace("validation", "http://www.mulesoft.org/schema/mule/validation");

  private XmlDslUtils() {
    // Nothing to do
  }

  /**
   * Assuming the value of {@code attr} is an expression, migrate it and update the value.
   *
   * @param attr         the attribute containing the expression to migrate
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
    appModel.addNameSpace(COMPATIBILITY_NAMESPACE, COMPATIBILITY_NS_SCHEMA_LOC, object.getDocument());

    int index = object.getParent().indexOf(object);
    buildAttributesToInboundProperties(report, object.getParent(), index + 1);

    if (expectsOutboundProperties) {
      Element errorHandlerElement = getFlowExcetionHandlingElement(object.getParentElement());
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
    addCompatibilityNamespace(appModel, object.getDocument());

    int index = object.getParent().indexOf(object);
    buildOutboundPropertiesToVar(report, object.getParent(), index, consumeStreams);
    if (outputsAttributes) {
      buildAttributesToInboundProperties(report, object.getParent(), index + 2);
    }
  }

  public static void migrateEnrichers(Element object, ExpressionMigrator expressionMigrator,
                                      CompatibilityResolver<String> resolver, ApplicationModel model,
                                      MigrationReport report) {
    String targetValue = object.getAttributeValue("target");
    if (isNotBlank(targetValue)) {
      String migratedExpression = expressionMigrator.migrateExpression(targetValue, true, object);
      object.setAttribute("target", expressionMigrator.unwrap(migratedExpression));
      if (resolver.canResolve(expressionMigrator.unwrap(targetValue))) {
        addOutboundPropertySetter(expressionMigrator.unwrap(migratedExpression), object, model, object);
        report.report(WARN, object, object, "Setting outbound property as variable",
                      "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");
      }
    }
  }

  public static Element addOutboundPropertySetter(String propertyName, Element element, ApplicationModel model,
                                                  Element after) {
    addCompatibilityNamespace(model, element.getDocument());
    Element setProperty = new Element("set-property", Namespace.getNamespace("compatibility", COMPATIBILITY_NAMESPACE.getURI()));
    setProperty.setAttribute(new Attribute("propertyName", propertyName));
    setProperty.setAttribute(new Attribute("value", "#[vars." + propertyName + "]"));

    addElementAfter(setProperty, after);
    return setProperty;
  }

  private static Element buildAttributesToInboundProperties(MigrationReport report, Parent parent, int index) {
    Element a2ip = new Element("attributes-to-inbound-properties", COMPATIBILITY_NAMESPACE);
    parent.addContent(index, a2ip);

    report.report(WARN, a2ip, a2ip,
                  "Expressions that query inboundProperties from the message should instead query the attributes of the message."
                      + lineSeparator()
                      + "Remove this component when there are no remaining usages of inboundProperties in expressions or components that rely on inboundProperties (such as copy-properties)",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#inbound-properties-are-now-attributes");
    return a2ip;
  }

  private static Element buildOutboundPropertiesToVar(MigrationReport report, Parent parent, int index, boolean consumeStreams) {
    Element op2v = new Element("outbound-properties-to-var", COMPATIBILITY_NAMESPACE);

    if (consumeStreams) {
      op2v.setAttribute("consumeStreams", "true");
    }

    parent.addContent(index, op2v);

    report.report(WARN, op2v, op2v,
                  "Instead of setting outbound properties in the flow, its values must be set explicitly in the operation/listener.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");

    return op2v;
  }

  /**
   * Add the required compatibility namespace declaration on document.
   */
  public static void addCompatibilityNamespace(ApplicationModel appModel, Document document) {
    appModel.addNameSpace(COMPATIBILITY_NAMESPACE, COMPATIBILITY_NS_SCHEMA_LOC, document);
  }

  /**
   * @param source        the element to remove the attribute from
   * @param target        the element to add the element to
   * @param attributeName the name of the attribute to move from source to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String attributeName) {
    return copyAttributeIfPresent(source, target, attributeName, attributeName);
  }

  /**
   * @param source              the element to remove the attribute from
   * @param target              the element to add the element to
   * @param sourceAttributeName the name of the attribute to remove from source
   * @param targetAttributeName the name of the attribute to add to target
   * @return {@code true} if the attribute was present on {@code source}, {@code false} otherwise
   */
  public static boolean copyAttributeIfPresent(final Element source, final Element target, final String sourceAttributeName,
                                               final String targetAttributeName) {
    if (source.getAttribute(sourceAttributeName) != null) {
      target.setAttribute(targetAttributeName, source.getAttributeValue(sourceAttributeName));
      source.removeAttribute(sourceAttributeName);
      return true;
    } else {
      return false;
    }
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

  public static Element getFlow(Element processor) {
    while (processor != null && !"flow".equals(processor.getName()) && !"sub-flow".equals(processor.getName())) {
      processor = processor.getParentElement();
    }

    return processor;
  }

  public static void addValidationModule(ApplicationModel applicationModel) {
    applicationModel.getPomModel().ifPresent(pom -> pom.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-validation-module")
        .withVersion("1.2.0")
        .withClassifier("mule-plugin")
        .build()));

    applicationModel.addNameSpace("validation", "http://www.mulesoft.org/schema/mule/validation",
                                  "http://www.mulesoft.org/schema/mule/validation/current/mule-validation.xsd");
  }

  public static boolean isTopLevelElement(Element element) {
    return (element.getParentElement().equals(element.getDocument().getRootElement()));
  }

  public static void createErrorHandlerParent(Element element) {
    Element parent = element.getParentElement();
    parent.removeContent(element);

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

  public static void addMigrationAttributeToElement(Element element, Attribute attribute) {
    attribute.setNamespace(Namespace.getNamespace("migration", "migration"));
    element.setAttribute(attribute);
  }

  public static boolean isErrorHanldingElement(Element element) {
    return element.getName()
        .matches("choice-exception-strategy|catch-exception-strategy|rollback-exception-strategy|exception-strategy|error-handler");
  }

  public static Element getFlowExcetionHandlingElement(Element flow) {
    return flow.getChildren().stream().filter(e -> isErrorHanldingElement(e)).findFirst().orElse(null);
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
}
