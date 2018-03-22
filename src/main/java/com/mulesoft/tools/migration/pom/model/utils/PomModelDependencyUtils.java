/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom.model.utils;

import com.mulesoft.tools.migration.pom.model.PomModelDependency;
import org.apache.maven.model.Dependency;

/**
 * Some helper functions to manage pom model dependencies
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PomModelDependencyUtils {

  public static Dependency toDependency(PomModelDependency pomModelDependency) {
    Dependency dependency = new Dependency();
    dependency.setArtifactId(pomModelDependency.getArtifactId());
    dependency.setGroupId(pomModelDependency.getGroupId());
    dependency.setVersion(pomModelDependency.getVersion());
    dependency.setClassifier(pomModelDependency.getClassifier());
    dependency.setType(pomModelDependency.getType());
    dependency.setScope(pomModelDependency.getScope());
    return dependency;
  }

  public static PomModelDependency toPomModelDependency(Dependency dependency) {
    PomModelDependency pomModelDependency = new PomModelDependency();
    pomModelDependency.setArtifactId(dependency.getArtifactId());
    pomModelDependency.setGroupId(dependency.getGroupId());
    pomModelDependency.setVersion(dependency.getVersion());
    pomModelDependency.setClassifier(dependency.getClassifier());
    pomModelDependency.setType(dependency.getType());
    pomModelDependency.setScope(dependency.getScope());
    return pomModelDependency;
  }
}
