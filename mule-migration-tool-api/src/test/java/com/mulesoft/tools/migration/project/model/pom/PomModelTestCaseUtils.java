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
