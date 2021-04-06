/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.spring;

import static com.google.common.collect.Lists.newArrayList;
import static org.jdom2.Namespace.getNamespace;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Parent;

import com.mulesoft.tools.migration.library.mule.steps.spring.AbstractSpringMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Migrates the spring beans imports.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveSpringBeansImport extends AbstractSpringMigratorStep {

  private static final String SPRING_BEANS_NS_PREFIX = "spring";
  public static final String SPRING_BEANS_NS_URI = "http://www.springframework.org/schema/beans";
  private static final Namespace SPRING_BEANS_NS = getNamespace(SPRING_BEANS_NS_PREFIX, SPRING_BEANS_NS_URI);
  public static final String XPATH_SELECTOR =
      "/*[starts-with(namespace-uri(), 'http://www.mulesoft.org/schema/mule/')]/*[namespace-uri() = '" + SPRING_BEANS_NS_URI
          + "' and local-name() = 'beans']";

  @Override
  public String getDescription() {
    return "Migrates the spring beans imports.";
  }

  public RemoveSpringBeansImport() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SPRING_BEANS_NS));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    List<Element> childrens = object.getChildren();
    List<Element> toBeRemoved = new ArrayList<Element>();
    for (Element element : childrens) {

      /**
       * <spring:import resource="classpath:api-main-before-apikit.xml" />
       * <spring:import resource="classpath:api-main-after-apikit.xml" />
       * <spring:import resource="classpath:global-exception-strategy-rest.xml" />
       */

      Attribute attribute = element.getAttribute("resource");
      if (attribute != null && (attribute.getValue().equals("classpath:api-main-before-apikit.xml")
          || attribute.getValue().equals("classpath:api-main-after-apikit.xml")
          || attribute.getValue().equals("classpath:global-exception-strategy-rest.xml"))) {
        toBeRemoved.add(element);
      }
    }
    if (childrens.size() == toBeRemoved.size()) {
      Parent parent = object.getParent();
      parent.removeContent(object);

    } else {
      for (Element element : toBeRemoved) {
        object.removeContent(element);
      }
    }

  }
}
