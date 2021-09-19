/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.pom;

/**
 * Represents a Parent in the pom model. By default its type is jar.
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Parent {

  /**
   * The ParentBuilder. It builds the Parent Wrapper.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class ParentBuilder {

    private String groupId;
    private String artifactId;
    private String version;

    public ParentBuilder withGroupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public ParentBuilder withArtifactId(String artifactId) {
      this.artifactId = artifactId;
      return this;
    }

    public ParentBuilder withVersion(String version) {
      this.version = version;
      return this;
    }

    /**
     * Builds the a Parent for a PomModel.
     *
     * @return a Parent
     */
    public Parent build() {
      Parent parent = new Parent();
      parent.setArtifactId(artifactId);
      parent.setGroupId(groupId);
      parent.setVersion(version);
      return parent;
    }
  }

  private final org.apache.maven.model.Parent parent;

  protected Parent(org.apache.maven.model.Parent parent) {
    this.parent = parent;
  }

  /**
   * Retrieves the parent model represented by a maven core object.
   *
   * @return the parent inner model
   */
  protected org.apache.maven.model.Parent getInnerModel() {
    return parent;
  }

  protected Parent() {
    this.parent = new org.apache.maven.model.Parent();
  }

  /**
   * Retrieves the pom parent artifact id.
   *
   * @return a {@link String}
   */
  public String getArtifactId() {
    return parent.getArtifactId();
  }

  /**
   * Retrieves the parent group id.
   *
   * @return a {@link String}
   */
  public String getGroupId() {
    return parent.getGroupId();
  }

  /**
   * Retrieves the parent version.
   *
   * @return a {@link String}
   */
  public String getVersion() {
    return parent.getVersion();
  }

  /**
   * Retrieves the parent relativePath version.
   *
   * @return a {@link String}
   */
  public String getRelativePath() {
    return parent.getRelativePath();
  }

  /**
   * Sets the parent artifact id.
   *
   * @param artifactId
   */
  public void setArtifactId(String artifactId) {

    parent.setArtifactId(artifactId);
  }

  /**
   * Sets the parent group id.
   *
   * @param groupId
   */
  public void setGroupId(String groupId) {
    parent.setGroupId(groupId);
  }

  /**
   * Sets the parent version.
   *
   * @param version
   */
  public void setVersion(String version) {
    parent.setVersion(version);
  }

  /**
   * Retrieves the parent relativePath.
   *
   * @return a {@link String}
   */
  public void setRelativePath(String relativePath) {
    parent.setRelativePath(relativePath);
  }

}
