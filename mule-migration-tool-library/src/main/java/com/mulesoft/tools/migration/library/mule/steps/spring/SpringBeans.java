/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.google.common.collect.Lists.newArrayList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the spring beans form the mule config to its own file.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringBeans extends AbstractSpringMigratorStep {

  private static final String SPRING_BEANS_NS_PREFIX = "spring";
  public static final String SPRING_BEANS_NS_URI = "http://www.springframework.org/schema/beans";
  private static final Namespace SPRING_BEANS_NS = getNamespace(SPRING_BEANS_NS_PREFIX, SPRING_BEANS_NS_URI);
  public static final String XPATH_SELECTOR =
      "/*[starts-with(namespace-uri(), 'http://www.mulesoft.org/schema/mule/')]/*[namespace-uri() = '" + SPRING_BEANS_NS_URI
          + "' and local-name() != 'beans']";

  @Override
  public String getDescription() {
    return "Migrates the spring beans form the mule config to its own file.";
  }

  public SpringBeans() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SPRING_BEANS_NS));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Document muleDocuemnt = object.getDocument();
    Document springDocument = resolveSpringDocument(muleDocuemnt);

    object.detach();
    springDocument.getRootElement().addContent(object);

    moveNamespacesDeclarations(muleDocuemnt, object, springDocument);
  }

}
