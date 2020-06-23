/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
