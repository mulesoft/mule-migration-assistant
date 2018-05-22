/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.addSharedLibs;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Ensures the packages of bean definitions are exported by the app.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExportSpringBeanPackages extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='http://www.springframework.org/schema/beans' and local-name()='bean']";

  @Override
  public String getDescription() {
    return "Ensures the packages of bean definitions are exported by the app.";
  }

  public ExportSpringBeanPackages() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (object.getAttribute("class") != null) {
      String className = object.getAttributeValue("class");
      String packageName = className.substring(0, className.lastIndexOf("."));

      // If we can identify the dependency containing a class used in spring, add it as shared library, otherwise export the
      // package
      if (packageName.startsWith("org.enhydra.jdbc")) {
        Dependency xaPool = new DependencyBuilder()
            .withGroupId("com.experlog")
            .withArtifactId("xapool")
            .withVersion("1.5.0")
            .build();
        getApplicationModel().getPomModel().get().addDependency(xaPool);
        addSharedLibs(getApplicationModel().getPomModel().get(), xaPool);
      } else {
        getApplicationModel().getMuleArtifactJsonModel().ifPresent(m -> m.addExportedPackage(packageName));
      }
    }
  }

}
