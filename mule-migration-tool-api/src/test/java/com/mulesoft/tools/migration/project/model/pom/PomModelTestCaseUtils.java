/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.pom;

import java.util.HashSet;
import java.util.Set;

public class PomModelTestCaseUtils {

  public static final String ARTIFACT_ID = "artifact-id";
  public static final String GROUP_ID = "group.id";
  public static final String VERSION = "3.0.0";

  public static Dependency getEmptyDependency() {
    return new Dependency();
  }

  public static Set<Dependency> getPomModelDependencies() {
    Dependency dep1 = buildDependency("org.dependency", "dependency", "1.0.0", null);
    Dependency dep2 = buildDependency("org.runtime.dependency", "runtime-dependency", "1.0.0", "runtime");
    Dependency dep3 = buildDependency("org.compile.dependency", "compile-dependency", "1.0.0", "compile");
    Dependency dep4 = buildDependency("org.test.dependency", "test-dependency", "1.0.0", "test");

    Set<Dependency> pomModelDependencies = new HashSet<>(4);
    pomModelDependencies.add(dep1);
    pomModelDependencies.add(dep2);
    pomModelDependencies.add(dep3);
    pomModelDependencies.add(dep4);

    return pomModelDependencies;
  }

  public static Dependency buildDependency(String groupId, String artifactId, String version, String scope) {
    return new Dependency.DependencyBuilder()
        .withGroupId(groupId)
        .withArtifactId(artifactId)
        .withVersion(version)
        .withScope(scope)
        .build();
  }
}
