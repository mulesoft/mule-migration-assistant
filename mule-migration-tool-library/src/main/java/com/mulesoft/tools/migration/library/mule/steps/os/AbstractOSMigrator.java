/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringBeans.SPRING_BEANS_NS_URI;
import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.setText;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates operations of the OS Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractOSMigrator extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  protected static final String OS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/objectstore";
  protected static final String OS_SCHEMA = "http://www.mulesoft.org/schema/mule/objectstore/current/mule-objectstore.xsd";
  protected static final String OS_NAMESPACE_PREFIX = "objectstore";
  protected static final Namespace OS_NAMESPACE = Namespace.getNamespace(OS_NAMESPACE_PREFIX, OS_NAMESPACE_URI);
  protected static final String NEW_OS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/os";
  protected static final String NEW_OS_SCHEMA = "http://www.mulesoft.org/schema/mule/os/current/mule-os.xsd";
  protected static final String NEW_OS_NAMESPACE_PREFIX = "os";
  protected static final Namespace NEW_OS_NAMESPACE = Namespace.getNamespace(NEW_OS_NAMESPACE_PREFIX, NEW_OS_NAMESPACE_URI);

  private ExpressionMigrator expressionMigrator;

  protected void migrateOS(Element element) {
    getApplicationModel().removeNameSpace(OS_NAMESPACE, OS_SCHEMA, element.getDocument());
    getApplicationModel().addNameSpace(NEW_OS_NAMESPACE, NEW_OS_SCHEMA, element.getDocument());
    element.setNamespace(NEW_OS_NAMESPACE);

    migrateConnection(element);
  }

  protected void migrateConnection(Element element) {
    Attribute config = element.getAttribute("config-ref");
    if (config != null) {
      config.setName("objectStore");
      Element osBean = getApplicationModel().getNodeOptional("//*[namespace-uri()='" + SPRING_BEANS_NS_URI
          + "' and local-name()='bean' and @id = '" + config.getValue() + "']").orElse(null);
      if (osBean != null) {
        osBean.detach();
        Element osConfig = new Element("object-store", NEW_OS_NAMESPACE);
        osConfig.setAttribute("name", config.getValue());
        osConfig.setAttribute("persistent", "false");
        addTopLevelElement(osConfig, element.getDocument());
      }
    }
  }

  protected void addNewRetrieveOperation(Element element, int position) {
    Element retrieveOperation = new Element("retrieve", NEW_OS_NAMESPACE);
    retrieveOperation.setAttribute(new Attribute("key", element.getAttributeValue("key")));
    retrieveOperation.setAttribute(new Attribute("objectStore", element.getAttributeValue("config-ref")));
    Attribute defaultValue = element.getAttribute("defaultValue-ref");
    if (defaultValue != null) {
      setOSValue(retrieveOperation, getExpressionMigrator().migrateExpression(defaultValue.getValue(), true, element),
                 "default-value");
    }
    element.getParentElement().addContent(position, retrieveOperation);
  }

  protected void addNewStoreOperation(Element element, int position) {
    Element storeOperation = new Element("store", NEW_OS_NAMESPACE);
    storeOperation.setAttribute(new Attribute("key", element.getAttributeValue("key")));
    storeOperation.setAttribute(new Attribute("objectStore", element.getAttributeValue("config-ref")));
    Attribute defaultValue = element.getAttribute("defaultValue-ref");
    if (defaultValue != null) {
      setOSValue(storeOperation, getExpressionMigrator().migrateExpression(defaultValue.getValue(), true, element), "value");
    }
    element.getParentElement().addContent(position, storeOperation);
  }

  protected void setOSValue(Element os, String value, String childName) {
    Element childValue = new Element(childName, NEW_OS_NAMESPACE);
    setText(childValue, value);
    os.addContent(childValue);
  }

  protected void addOSModule() {
    getApplicationModel().getPomModel().ifPresent(pom -> pom.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("org.mule.connectors")
        .withArtifactId("mule-objectstore-connector")
        .withVersion(targetVersion("mule-objectstore-connector"))
        .withClassifier("mule-plugin")
        .build()));

    getApplicationModel().addNameSpace(NEW_OS_NAMESPACE_PREFIX, NEW_OS_NAMESPACE_URI, NEW_OS_SCHEMA);
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
