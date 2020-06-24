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
 * Migrates the security-manager element.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurityManager extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri()='http://www.mulesoft.org/schema/mule/spring-security' and (local-name()='security-manager')]";

  @Override
  public String getDescription() {
    return "Migrates the security-manager element.";
  }

  public SecurityManager() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(SPRING_SECURITY_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(SPRING_NAMESPACE);
    for (Element provider : object.getChildren("delegate-security-provider", SPRING_SECURITY_NAMESPACE)) {
      provider.setNamespace(SPRING_NAMESPACE);
      for (Element property : provider.getChildren("security-property", SPRING_SECURITY_NAMESPACE)) {
        property.setNamespace(SPRING_NAMESPACE);
      }
    }
  }

}
