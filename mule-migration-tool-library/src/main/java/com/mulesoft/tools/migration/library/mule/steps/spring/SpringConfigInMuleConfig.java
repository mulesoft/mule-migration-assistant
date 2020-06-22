/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringBeans.SPRING_BEANS_NS_URI;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;

/**
 * Migrates the spring configuration containing a mule config to its own file.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringConfigInMuleConfig extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "/*[starts-with(namespace-uri(), 'http://www.mulesoft.org/schema/mule/')]"
          + "/*[namespace-uri()='" + SPRING_BEANS_NS_URI + "' and local-name()='beans']";

  @Override
  public String getDescription() {
    return "Migrates the spring beans configuration form the mule config to its own file.";
  }

  public SpringConfigInMuleConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Document muleDocument = object.getDocument();
    Document springDocument = resolveSpringDocument(muleDocument);

    for (Element element : new ArrayList<>(object.getChildren())) {
      element.detach();
      springDocument.getRootElement().addContent(element);
      moveNamespacesDeclarations(muleDocument, element, springDocument);
    }

    object.detach();
  }

}
