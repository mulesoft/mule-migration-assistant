/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom.model.predicate;

import com.mulesoft.tools.migration.pom.model.PomModelDependency;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A dependency predicate used to recognize if a dependency can be added to the pom dependencies, or to filter some of them.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DependencyPredicate {

  private final PomModelDependency pomModelDependency;

  public DependencyPredicate(PomModelDependency pomModelDependency) {
    this.pomModelDependency = pomModelDependency;
  }

  public Predicate<Dependency> isSameDependency() {
    return notNull().and(notNull(pomModelDependency)).and(hasSameArtifactId()).and(hasSameGroupId()).and(hasSameVersion());
  }

  protected Predicate<Dependency> notNull() {
    return Objects::nonNull;
  }

  protected Predicate<Dependency> notNull(PomModelDependency dependency) {
    return d -> dependency != null;
  }

  protected Predicate<Dependency> hasSameArtifactId() {
    return d -> d.getArtifactId() == null || pomModelDependency.getArtifactId() == null
        || d.getArtifactId().equals(pomModelDependency.getArtifactId());
  }

  Predicate<Dependency> hasSameGroupId() {
    return d -> d.getGroupId() == null || pomModelDependency.getGroupId() == null
        || d.getGroupId().equals(pomModelDependency.getGroupId());
  }

  Predicate<Dependency> hasSameVersion() {
    return d -> d.getVersion() == null || pomModelDependency.getVersion() == null
        || d.getVersion().equals(pomModelDependency.getVersion());
  }
}
