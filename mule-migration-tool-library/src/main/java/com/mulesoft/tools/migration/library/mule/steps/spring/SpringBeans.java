/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Migrates the spring beans form the mule config to its own file.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringBeans extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "/mule:mule/*[namespace-uri()='http://www.springframework.org/schema/beans' and local-name()!='beans']";

  @Override
  public String getDescription() {
    return "Migrates the spring beans form the mule config to its own file.";
  }

  public SpringBeans() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Document muleDocuemnt = object.getDocument();
    Document springDocument = resolveSpringDocument(muleDocuemnt);

    object.detach();
    springDocument.getRootElement().addContent(object);

    moveNamespacesDeclarations(muleDocuemnt, object, springDocument);
  }

  // spring:bean or spring:property nested in cxf
  // spring-security?


}
