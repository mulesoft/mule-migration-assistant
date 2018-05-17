/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.springsecurity;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the security-manager definition.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurityManager extends AbstractSpringSecurityMigratorStep {

  public static final String XPATH_SELECTOR =
      "/mule:mule/*[namespace-uri()='http://www.mulesoft.org/schema/mule/spring-security' and local-name()='security-manager']";

  @Override
  public String getDescription() {
    return "Migrates the security-manager definition.";
  }

  public SecurityManager() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(SPRING_MODULE_NAMESPACE);
    for (Element provider : object.getChildren("delegate-security-provider", SPRING_SECURITY_NAMESPACE)) {
      provider.setNamespace(SPRING_MODULE_NAMESPACE);
      for (Element property : provider
          .getChildren("security-property", SPRING_SECURITY_NAMESPACE)) {
        property.setNamespace(SPRING_MODULE_NAMESPACE);
      }
    }

  }

}
