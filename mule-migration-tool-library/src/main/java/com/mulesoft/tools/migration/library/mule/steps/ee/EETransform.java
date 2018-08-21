/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.migrateDWToV2;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate EE Transform DW 1.0 Script to DW 2.0
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class EETransform extends AbstractApplicationModelMigrationStep {

  private static final String DW_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/dw";
  private static final String DW_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/dw/current/dw.xsd";
  private static final String EE_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + DW_NAMESPACE_URI + "'"
      + " and local-name()='transform-message']";

  @Override
  public String getDescription() {
    return "Migrate EE Transform DW 1.0 Script to DW 2.0";
  }

  public EETransform() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace("dw", DW_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Namespace eeNamespace = CORE_EE_NAMESPACE;

    getApplicationModel().addNameSpace(eeNamespace, EE_NAMESPACE_SCHEMA, element.getDocument());
    getApplicationModel().removeNameSpace(getNamespace("dw", DW_NAMESPACE_URI), DW_NAMESPACE_SCHEMA, element.getDocument());
    element.setNamespace(eeNamespace);
    element.setName("transform");

    Element messageNode = new Element("message", element.getNamespace());
    Element variablesNode = new Element("variables", element.getNamespace());

    List<Element> transformerNodes = new ArrayList<>(element.getChildren());
    transformerNodes.forEach(n -> {
      n.setNamespace(element.getNamespace());
      migrateDWScript(n);
      if ("set-payload".equals(n.getName())) {
        n.detach();
        messageNode.addContent(n);
      } else {
        moveToVariablesSection(n, variablesNode, report);
      }
    });

    if (messageNode.getChildren().size() > 0) {
      element.addContent(messageNode);
    }

    if (variablesNode.getChildren().size() > 0) {
      element.addContent(variablesNode);
    }
  }

  private void moveToVariablesSection(Element element, Element variablesSection, MigrationReport report) {
    if ("set-session-variable".equals(element.getName())) {
      addSessionVariable(element, report);
    } else if ("set-property".equals(element.getName())) {
      Attribute propName = element.getAttribute("propertyName");
      propName.setName("variableName");
      addOutboundProperty(element, report);
    }
    element.setName("set-variable");
    element.detach();
    variablesSection.addContent(element);
  }

  private void addSessionVariable(Element element, MigrationReport report) {
    addCompatibilityNamespace(element.getDocument());
    Element sessionVar = new Element("set-session-variable", COMPATIBILITY_NAMESPACE);
    Attribute varName = element.getAttribute("variableName");
    sessionVar.setAttribute(new Attribute(varName.getName(), varName.getValue()));
    sessionVar.setAttribute(new Attribute("value", "#[vars." + varName.getValue() + "]"));

    addElementAfter(sessionVar, element.getParentElement());

    report.report(WARN, sessionVar, sessionVar,
                  "Instead of setting session variables in the flow, you can set Variables.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-manual#session_variables");
  }

  private void addOutboundProperty(Element element, MigrationReport report) {
    Attribute propName = element.getAttribute("variableName");
    Element setProperty =
        XmlDslUtils.addOutboundPropertySetter(propName.getValue(), element, getApplicationModel(), element.getParentElement());
    report.report(WARN, setProperty, setProperty,
                  "Instead of setting outbound properties in the flow, you can set Variables.",
                  "https://docs.mulesoft.com/mule-user-guide/v/4.1/migration-manual#outbound_properties");
  }

  private void migrateDWScript(Element element) {
    if (!StringUtils.isEmpty(element.getText())) {
      String migratedScript = migrateDWToV2(element.getText());
      element.removeContent();
      element.addContent(new CDATA(migratedScript));
    } else if (element.getAttribute("resource") != null) {
      Attribute resourceAttr = element.getAttribute("resource");
      String resourceValue = resourceAttr.getValue();
      if (resourceValue.startsWith("classpath:")) {
        resourceValue = resourceValue.replace("classpath:", "");
        resourceAttr.setValue(resourceValue);
      }
    }
  }
}
