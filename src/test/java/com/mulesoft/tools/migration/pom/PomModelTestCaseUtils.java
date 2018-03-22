/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom;

import com.mulesoft.tools.migration.pom.model.PomModelDependency;

import java.util.HashSet;
import java.util.Set;

public class PomModelTestCaseUtils {

  static Set<PomModelDependency> getPomModelDependencies() {
    PomModelDependency dep1 = buildDependency("org.dependency", "dependency", "1.0.0", null);
    PomModelDependency dep2 = buildDependency("org.runtime.dependency", "runtime-dependency", "1.0.0", "runtime");
    PomModelDependency dep3 = buildDependency("org.compile.dependency", "compile-dependency", "1.0.0", "compile");
    PomModelDependency dep4 = buildDependency("org.test.dependency", "test-dependency", "1.0.0", "test");

    Set<PomModelDependency> pomModelDependencies = new HashSet<>(4);
    pomModelDependencies.add(dep1);
    pomModelDependencies.add(dep2);
    pomModelDependencies.add(dep3);
    pomModelDependencies.add(dep4);

    return pomModelDependencies;
  }

  static PomModelDependency buildDependency(String groupId, String artifactId, String version, String scope) {
    PomModelDependency dependency = new PomModelDependency();
    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    dependency.setVersion(version);
    if (scope != null) {
      dependency.setScope(scope);
    }
    return dependency;
  }
}
