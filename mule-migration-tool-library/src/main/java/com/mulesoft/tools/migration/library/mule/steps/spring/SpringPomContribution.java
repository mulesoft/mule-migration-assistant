/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

import org.codehaus.plexus.util.xml.Xpp3Dom;

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
        .withVersion("1.1.1")
        .withClassifier("mule-plugin")
        .build());

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-core")
        .withVersion(SPRING_VERSION)
        .build());

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-beans")
        .withVersion(SPRING_VERSION)
        .build());

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-context")
        .withVersion(SPRING_VERSION)
        .build());

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.springframework")
        .withArtifactId("spring-aop")
        .withVersion(SPRING_VERSION)
        .build());

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.springframework.security")
        .withArtifactId("spring-security-core")
        .withVersion(SPRING_SECURITY_VERSION)
        .build());

    object.addDependency(new DependencyBuilder()
        .withGroupId("org.springframework.security")
        .withArtifactId("spring-security-config")
        .withVersion(SPRING_SECURITY_VERSION)
        .build());

    // add spring as shared libs
    object.getPlugins().stream()
        .filter(plugin -> "org.mule.tools.maven".equals(plugin.getGroupId())
            && "mule-maven-plugin".equals(plugin.getArtifactId()))
        .findFirst().map(p -> {
          Xpp3Dom configuration = p.getConfiguration();
          Xpp3Dom sharedLibraries = configuration.getChild("sharedLibraries");
          if (sharedLibraries == null) {
            sharedLibraries = new Xpp3Dom("sharedLibraries");
            p.getConfiguration().addChild(sharedLibraries);
          }

          return sharedLibraries;
        }).ifPresent(sharedLibraries -> {
          addSharedLib(sharedLibraries, "org.springframework", "spring-core");
          addSharedLib(sharedLibraries, "org.springframework", "spring-beans");
          addSharedLib(sharedLibraries, "org.springframework", "spring-context");
          addSharedLib(sharedLibraries, "org.springframework", "spring-aop");
          addSharedLib(sharedLibraries, "org.springframework.security", "spring-security-core");
          addSharedLib(sharedLibraries, "org.springframework.security", "spring-security-config");
        });

  }

  private void addSharedLib(Xpp3Dom sharedLibraries, String groupId, String artifactId) {
    Xpp3Dom sharedLib = new Xpp3Dom("sharedLibrary");
    Xpp3Dom groupIdNode = new Xpp3Dom("groupId");
    groupIdNode.setValue(groupId);
    sharedLib.addChild(groupIdNode);
    Xpp3Dom artifactIdNode = new Xpp3Dom("artifactId");
    sharedLib.addChild(artifactIdNode);
    artifactIdNode.setValue(artifactId);
    sharedLibraries.addChild(sharedLib);
  }

}
