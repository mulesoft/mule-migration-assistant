/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
