/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static java.util.Collections.singletonList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the authorization-filter element.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AuthorizationFilter extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='http://www.mulesoft.org/schema/mule/spring-security' and local-name()='authorization-filter']";

  @Override
  public String getDescription() {
    return "Migrates the authorization-filter element.";
  }

  public AuthorizationFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(SPRING_SECURITY_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(SPRING_NAMESPACE);
  }

}
