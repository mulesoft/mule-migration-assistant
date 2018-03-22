/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom.model;

/**
 * Represents a dependency in the pom model. By default its type is jar.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PomModelDependency {

  private static final String DEFAULT_TYPE = "jar";
  private String artifactId;
  private String groupId;
  private String version;
  private String classifier;
  private String type;
  private String scope;

  public String getArtifactId() {
    return artifactId;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getVersion() {
    return version;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getType() {
    return type != null ? type : DEFAULT_TYPE;
  }

  public String getScope() {
    return scope;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PomModelDependency that = (PomModelDependency) o;

    if (getArtifactId() != null ? !getArtifactId().equals(that.getArtifactId()) : that.getArtifactId() != null) {
      return false;
    }
    if (getGroupId() != null ? !getGroupId().equals(that.getGroupId()) : that.getGroupId() != null) {
      return false;
    }
    if (getVersion() != null ? !getVersion().equals(that.getVersion()) : that.getVersion() != null) {
      return false;
    }
    if (getClassifier() != null ? !getClassifier().equals(that.getClassifier()) : that.getClassifier() != null) {
      return false;
    }
    if (!getType().equals(that.getType())) {
      return false;
    }
    return getScope() != null ? getScope().equals(that.getScope()) : that.getScope() == null;
  }

  @Override
  public int hashCode() {
    int result = getArtifactId() != null ? getArtifactId().hashCode() : 0;
    result = 31 * result + (getGroupId() != null ? getGroupId().hashCode() : 0);
    result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
    result = 31 * result + (getClassifier() != null ? getClassifier().hashCode() : 0);
    result = 31 * result + getType().hashCode();
    result = 31 * result + (getScope() != null ? getScope().hashCode() : 0);
    return result;
  }
}
