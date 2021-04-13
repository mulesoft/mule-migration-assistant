/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

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
