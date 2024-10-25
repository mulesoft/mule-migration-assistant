/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.ee;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.core.dw.DataWeaveHelper.migrateDWToV2;
import static com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator.OUTBOUND_PREFIX;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addCompatibilityNamespace;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addOutboundPropertySetter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate EE Transform DW 1.0 Script to DW 2.0
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class EETransform extends AbstractApplicationModelMigrationStep {

  private static final String DW_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ee/dw";
  private static final Namespace DW_NAMESPACE = getNamespace("dw", DW_NAMESPACE_URI);
  private static final String DW_NAMESPACE_SCHEMA = "http://www.mulesoft.org/schema/mule/ee/dw/current/dw.xsd";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + DW_NAMESPACE_URI + "'"
      + " and local-name()='transform-message']";

  @Override
  public String getDescription() {
    return "Migrate EE Transform DW 1.0 Script to DW 2.0";
  }

  public EETransform() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(DW_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Namespace eeNamespace = CORE_EE_NAMESPACE;

    getApplicationModel().addNameSpace(eeNamespace, EE_NAMESPACE_SCHEMA, element.getDocument());
    getApplicationModel().removeNameSpace(DW_NAMESPACE, DW_NAMESPACE_SCHEMA, element.getDocument());
    element.setNamespace(eeNamespace);
    element.setName("transform");

    Element messageNode = new Element("message", element.getNamespace());
    Element variablesNode = new Element("variables", element.getNamespace());

    List<Element> transformerNodes = new ArrayList<>(element.getChildren());
    transformerNodes.forEach(n -> {
      if ("input-payload".equals(n.getName())) {
        addElementBefore(new Element("set-payload", CORE_NAMESPACE)
            .setAttribute("value", "#[payload]")
            .setAttribute("mimeType", n.getAttributeValue("mimeType") + readerPropsToMimeTypeParams(n)), element);

        n.detach();
      } else if ("input-variable".equals(n.getName())) {
        addElementBefore(new Element("set-variable", CORE_NAMESPACE)
            .setAttribute("variableName", n.getAttributeValue("variableName"))
            .setAttribute("value", "#[vars." + n.getAttributeValue("variableName") + "]")
            .setAttribute("mimeType", n.getAttributeValue("mimeType") + readerPropsToMimeTypeParams(n)), element);

        n.detach();
      } else if ("input-session-variable".equals(n.getName())) {
        if (!getApplicationModel().noCompatibilityMode()) {
          addCompatibilityNamespace(element.getDocument());
          Element sessionVar = new Element("set-session-variable", COMPATIBILITY_NAMESPACE)
              .setAttribute("variableName", n.getAttributeValue("variableName"))
              .setAttribute("value", "#[vars." + n.getAttributeValue("variableName") + "]")
              .setAttribute("mimeType", n.getAttributeValue("mimeType") + readerPropsToMimeTypeParams(n));
          addElementBefore(sessionVar, element);
          report.report("transform.sessionVars", sessionVar, sessionVar);
        } else {
          report.report("transform.sessionVars", n, element);
        }
        n.detach();
      } else if ("input-inbound-property".equals(n.getName()) || "input-outbound-property".equals(n.getName())) {
        if (!getApplicationModel().noCompatibilityMode()) {
          Element setProperty = new Element("set-property", COMPATIBILITY_NAMESPACE)
              .setAttribute("propertyName", n.getAttributeValue("propertyName"))
              .setAttribute("value", "#[vars." + n.getAttributeValue("propertyName") + "]")
              .setAttribute("mimeType", n.getAttributeValue("mimeType") + readerPropsToMimeTypeParams(n));
          report.report("transform.outboundProperties", setProperty, setProperty);
        } else {
          report.report("transform.outboundProperties", n, element);
        }
        n.detach();
      } else {
        n.setNamespace(element.getNamespace());
        migrateDWScript(n, report);
        if ("set-payload".equals(n.getName())) {
          n.detach();
          messageNode.addContent(n);
        } else {
          moveToVariablesSection(n, variablesNode, report);
        }
      }
    });

    if (messageNode.getChildren().size() > 0) {
      element.addContent(messageNode);
    }

    if (variablesNode.getChildren().size() > 0) {
      element.addContent(variablesNode);
    }
  }

  private String readerPropsToMimeTypeParams(Element n) {
    StringBuilder readerMimeTypeParams = new StringBuilder();
    for (Element readerProp : n.getChildren("reader-property", DW_NAMESPACE)) {
      readerMimeTypeParams
          .append("; " + readerProp.getAttributeValue("name") + "=\"" + readerProp.getAttributeValue("value") + "\"");
    }
    return readerMimeTypeParams.toString();
  }

  private void moveToVariablesSection(Element element, Element variablesSection, MigrationReport report) {
    if ("set-session-variable".equals(element.getName()) && !getApplicationModel().noCompatibilityMode()) {
      addSessionVariable(element, report);
    } else if ("set-property".equals(element.getName())) {
      Attribute propName = element.getAttribute("propertyName");
      propName.setName("variableName");
      if (getApplicationModel().noCompatibilityMode()) {
        propName.setValue(OUTBOUND_PREFIX + propName.getValue());
      } else {
        addOutboundProperty(element, report);
      }
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

    report.report("transform.sessionVars", sessionVar, sessionVar);
  }

  private void addOutboundProperty(Element element, MigrationReport report) {
    Attribute propName = element.getAttribute("variableName");
    Element setProperty =
        addOutboundPropertySetter(propName.getValue(), element, getApplicationModel(), element.getParentElement());
    report.report("transform.outboundProperties", setProperty, setProperty);
  }

  private void migrateDWScript(Element element, MigrationReport report) {
    String sourceScript = element.getText();
    if (!StringUtils.isEmpty(sourceScript)) {
      try {
        String migratedScript = migrateDWToV2(sourceScript);
        element.removeContent();
        setText(element, migratedScript);
        report.dwTransformsSuccess(sourceScript);
      } catch (Exception ex) {
        report.report("dataWeave.migrationErrorScript", element, element, sourceScript, ex.getMessage());
        report.dwTransformsFailure(sourceScript);
      }
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
