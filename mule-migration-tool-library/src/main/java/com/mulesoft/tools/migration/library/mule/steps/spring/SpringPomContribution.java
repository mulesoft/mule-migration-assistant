/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.addSharedLibs;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the Spring Module dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringPomContribution implements PomContribution {

  private static final String SPRING_VERSION = "4.3.17.RELEASE";
  private static final String SPRING_SECURITY_VERSION = "4.2.6.RELEASE";

  @Override
  public String getDescription() {
    return "Add Spring Module dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-spring-module")
        .withVersion(targetVersion("mule-spring-module"))
        .withClassifier("mule-plugin")
        .build());

    Dependency springCore = new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-core")
        .withVersion(SPRING_VERSION)
        .build();
    object.addDependency(springCore);

    Dependency springBeans = new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-beans")
        .withVersion(SPRING_VERSION)
        .build();
    object.addDependency(springBeans);

    Dependency springContext = new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-context")
        .withVersion(SPRING_VERSION)
        .build();
    object.addDependency(springContext);

    Dependency springAop = new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-aop")
        .withVersion(SPRING_VERSION)
        .build();
    object.addDependency(springAop);

    Dependency springSecurityCore = new DependencyBuilder()
        .withGroupId("org.springframework.security")
        .withArtifactId("spring-security-core")
        .withVersion(SPRING_SECURITY_VERSION)
        .build();
    object.addDependency(springSecurityCore);

    Dependency springSecurityConfig = new DependencyBuilder()
        .withGroupId("org.springframework.security")
        .withArtifactId("spring-security-config")
        .withVersion(SPRING_SECURITY_VERSION)
        .build();
    object.addDependency(springSecurityConfig);

    addSharedLibs(object, springCore, springBeans, springContext, springAop, springSecurityCore, springSecurityConfig);
  }

}
