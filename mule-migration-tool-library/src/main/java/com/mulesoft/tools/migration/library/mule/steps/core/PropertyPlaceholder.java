/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;

/**
 * Migrate Property Placeholder component.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PropertyPlaceholder extends AbstractApplicationModelMigrationStep {

  private static final String SPRING_CONTEXT_NS_PREFIX = "context";
  private static final String SPRING_CONTEXT_NS_URI = "http://www.springframework.org/schema/context";
  private static final String SPRING_CONTEXT_SCHEMA = "http://www.springframework.org/schema/context/spring-context.xsd";
  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + SPRING_CONTEXT_NS_URI + "' and local-name()='property-placeholder']";

  @Override
  public String getDescription() {
    return "Migrate Property Placeholder component";
  }

  public PropertyPlaceholder() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    Attribute propsLocation = element.getAttribute("location");
    if (propsLocation != null) {
      Element configProperties = new Element("configuration-properties", CORE_NAMESPACE);
      configProperties.setAttribute("file", propsLocation.getValue());

      addTopLevelElement(configProperties, element.getDocument());
    }

    getApplicationModel().removeNameSpace(Namespace.getNamespace(SPRING_CONTEXT_NS_PREFIX, SPRING_CONTEXT_NS_URI),
                                          SPRING_CONTEXT_SCHEMA, element.getDocument());
    element.detach();
  }
}
