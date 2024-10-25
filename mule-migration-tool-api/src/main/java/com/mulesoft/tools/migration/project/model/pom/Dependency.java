/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.pom;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Represents a dependency in the pom model. By default its type is jar.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Dependency {

  private final org.apache.maven.model.Dependency dependency;

  protected Dependency(org.apache.maven.model.Dependency dependency) {
    this.dependency = dependency;
  }

  /**
   * Retrieves the dependency model represented by a maven core object. Meant to be used just by the classes in the package.
   *
   * @return the dependency inner model
   */
  protected org.apache.maven.model.Dependency getInnerModel() {
    return dependency;
  }

  protected Dependency() {
    this.dependency = new org.apache.maven.model.Dependency();
  }

  private static final String DEFAULT_TYPE = "jar";

  /**
   * Equals method implementation for a dependency. Two dependencies are considered equal if they have the same artifact id and group id.
   *
   * @param o the other dependency to be test by equality
   * @return true if the dependencies have the same artifact id and group id
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Dependency that = (Dependency) o;
    return Objects.equals(getArtifactId(), that.getArtifactId())
        && Objects.equals(getGroupId(), that.getGroupId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getArtifactId(), getGroupId());
  }

  /**
   * Retrieves the dependency artifact id.
   *
   * @return a {@link String}
   */
  public String getArtifactId() {
    return dependency.getArtifactId();
  }

  /**
   * Retrieves the dependency group id.
   *
   * @return a {@link String}
   */
  public String getGroupId() {
    return dependency.getGroupId();
  }

  /**
   * Retrieves the dependency version.
   *
   * @return a {@link String}
   */
  public String getVersion() {
    return dependency.getVersion();
  }

  /**
   * Retrieves the dependency classifier.
   *
   * @return a {@link String}
   */
  public String getClassifier() {
    return dependency.getClassifier();
  }

  /**
   * Retrieves the dependency type.
   *
   * @return a {@link String}
   */
  public String getType() {
    return dependency.getType() != null ? dependency.getType() : DEFAULT_TYPE;
  }

  /**
   * Retrieves the dependency scope.
   *
   * @return a {@link String}
   */
  public String getScope() {
    return dependency.getScope();
  }

  /**
   * Sets the dependency artifact id.
   *
   * @param artifactId
   */
  public void setArtifactId(String artifactId) {
    dependency.setArtifactId(artifactId);
  }

  /**
   * Sets the dependency group id.
   *
   * @param groupId
   */
  public void setGroupId(String groupId) {
    dependency.setGroupId(groupId);
  }

  /**
   * Sets the dependency version.
   *
   * @param version
   */
  public void setVersion(String version) {
    dependency.setVersion(version);
  }

  /**
   * Sets the dependency classifier.
   *
   * @param classifier
   */
  public void setClassifier(String classifier) {
    dependency.setClassifier(classifier);
  }

  /**
   * Sets the dependency type.
   *
   * @param type
   */
  public void setType(String type) {
    dependency.setType(type);
  }

  /**
   * Sets the dependency scope.
   *
   * @param scope
   */
  public void setScope(String scope) {
    dependency.setScope(scope);
  }

  /**
   * A builder of dependencies.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class DependencyBuilder {

    private String artifactId;
    private String groupId;
    private String version;
    private String classifier;
    private String type;
    private String scope;

    public DependencyBuilder withArtifactId(String artifactId) {
      this.artifactId = artifactId;
      return this;
    }

    public DependencyBuilder withGroupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public DependencyBuilder withVersion(String version) {
      this.version = version;
      return this;
    }

    public DependencyBuilder withClassifier(String classifier) {
      this.classifier = classifier;
      return this;
    }

    public DependencyBuilder withType(String type) {
      this.type = type;
      return this;
    }

    public DependencyBuilder withScope(String scope) {
      this.scope = scope;
      return this;
    }

    /**
     * Builds the dependency. Artifact id, group id and version are mandatory fields. Also, when declared, classifier, type and scope should not be empty strings.
     *
     * @return a dependency instance
     */
    public Dependency build() {
      checkArgument(isNotBlank(artifactId), "Artifact id cannot be null nor empty");
      checkArgument(isNotBlank(groupId), "Group id cannot be null nor empty");
      checkArgument(isNotBlank(version), "Version cannot be null nor empty");

      Dependency dependency = new Dependency();
      dependency.setArtifactId(artifactId);
      dependency.setGroupId(groupId);
      dependency.setVersion(version);

      if (classifier != null) {
        checkArgument(isNotBlank(classifier), "Classifier should not be blank");
        dependency.setClassifier(classifier);
      }

      if (type != null) {
        checkArgument(isNotBlank(type), "Type should not be blank");
        dependency.setType(type);
      }

      if (scope != null) {
        checkArgument(isNotBlank(scope), "Scope should not be blank");
        dependency.setScope(scope);
      }

      return dependency;
    }
  }


}
