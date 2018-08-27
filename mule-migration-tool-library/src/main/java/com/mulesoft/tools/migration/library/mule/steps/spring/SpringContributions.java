/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Migrates the rest of spring elements.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringContributions extends AbstractSpringMigratorStep {

  public static final String ADDITIONAL_SPRING_NAMESPACES_PROP = "mule.migration.additionalSpringNamespaces";

  private static final String ADDITIONAL_SPRING_NAMESPACES = System.getProperty(ADDITIONAL_SPRING_NAMESPACES_PROP);

  public static final String XPATH_SELECTOR =
      "/*[starts-with(namespace-uri(), 'http://www.mulesoft.org/schema/mule/')]/*[starts-with(namespace-uri(), 'http://www.springframework.org/schema') %s]";

  @Override
  public String getDescription() {
    return "Migrates the rest of spring elements.";
  }

  public SpringContributions() {
    if (ADDITIONAL_SPRING_NAMESPACES == null) {
      this.setAppliedTo(format(XPATH_SELECTOR, ""));
    } else {
      // The app may be using a lib that declares a Spring namespace handler and elements of that namespace may be present in the
      // app.
      // We move those elements to the spring file so that spring handles them.
      this.setAppliedTo(format(XPATH_SELECTOR, stream(ADDITIONAL_SPRING_NAMESPACES.split(","))
          .map(nsUri -> "namespace-uri() = '" + nsUri + "'").collect(joining(" or ", "or ", ""))));
    }
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Document muleDocument = object.getDocument();
    Document springDocument = resolveSpringDocument(muleDocument);

    object.detach();
    springDocument.getRootElement().addContent(object);
    moveNamespacesDeclarations(muleDocument, object, springDocument);
  }
}
