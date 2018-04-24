/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides reusable methods for common migration scenarios.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public final class XmlDslUtils {

  private static final String COMPATIBILITY_NS_URI = "http://www.mulesoft.org/schema/mule/compatibility";
  private static final String COMPATIBILITY_NS_SCHEMA_LOC =
      "http://www.mulesoft.org/schema/mule/compatibility/current/mule-compatibility.xsd";
  private static final String CORE_NS_URI = "http://www.mulesoft.org/schema/mule/core";

  public static final Namespace COMPATIBILITY_NAMESPACE = Namespace.getNamespace("compatibility", COMPATIBILITY_NS_URI);
  public static final Namespace CORE_NAMESPACE = Namespace.getNamespace(CORE_NS_URI);

  private XmlDslUtils() {
    // Nothing to do
  }

  /**
   * Assuming the value of {@code attr} is an expression, migrate it and update the value.
   *
   * @param attr the attribute containing the expression to migrate
   * @param exprMigrator the migrator for the expressions
   */
  public static void migrateExpression(Attribute attr, ExpressionMigrator exprMigrator) {
    if (attr != null && exprMigrator.isWrapped(attr.getValue())) {
      attr.setValue(exprMigrator.wrap(exprMigrator.migrateExpression(attr.getValue(), true)));
    }
  }

  /**
   *
   * @param doc
   * @param xPathExpression
   * @return
   */
  public static List<Element> getElementsFromDocument(Document doc, String xPathExpression) {
    List<Namespace> namespaces = new ArrayList<>();
    namespaces.add(Namespace.getNamespace("mule", CORE_NS_URI));
    namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
    return xpath.evaluate(doc);
  }

  /**
   * Add the required compatibility elements to the flow for a migrated source to work correctly.
   */
  public static void migrateSourceStructure(ApplicationModel appModel, Element object, MigrationReport report) {
    appModel.addNameSpace(COMPATIBILITY_NAMESPACE, COMPATIBILITY_NS_SCHEMA_LOC, object.getDocument());

    int index = object.getParent().indexOf(object);
    object.getParent().addContent(index + 1, buildAttributesToInboundProperties(report));
    object.getParent().addContent(buildOutboundPropertiesToVar(report));
  }

  /**
   * Add the required compatibility elements to the flow for a migrated operation to work correctly.
   */
  public static void migrateOperationStructure(ApplicationModel appModel, Element object, MigrationReport report) {
    appModel.addNameSpace(COMPATIBILITY_NAMESPACE, COMPATIBILITY_NS_SCHEMA_LOC, object.getDocument());

    int index = object.getParent().indexOf(object);
    object.getParent().addContent(index, buildOutboundPropertiesToVar(report));
    object.getParent().addContent(index + 2, buildAttributesToInboundProperties(report));
  }

  private static Element buildAttributesToInboundProperties(MigrationReport report) {
    Element a2ip = new Element("attributes-to-inbound-properties", COMPATIBILITY_NAMESPACE);

    report.report(WARN, a2ip, a2ip,
                  "Expressions that query inboundProperties from the message should instead query the attributes of the message."
                      + lineSeparator()
                      + "Remove this component when there are no remaining usages of inboundProperties in expressions or components that rely on inboundProperties (such as copy-properties)",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#inbound-properties-are-now-attributes");
    return a2ip;
  }

  private static Element buildOutboundPropertiesToVar(MigrationReport report) {
    Element op2v = new Element("outbound-properties-to-var", COMPATIBILITY_NAMESPACE);

    report.report(WARN, op2v, op2v,
                  "Instead of setting outbound properties in the flow, its values must be set explicitly in the operation/listener.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-mule-message#outbound-properties");

    return op2v;
  }
}
