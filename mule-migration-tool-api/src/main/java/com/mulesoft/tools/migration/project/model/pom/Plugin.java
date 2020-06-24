/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.pom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.model.pom.Plugin.PluginBuilder.areAllExecutionIdsUnique;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;
import java.util.Objects;

/**
 * Represents a plugin in the pom model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Plugin {

  private final org.apache.maven.model.Plugin plugin;

  protected Plugin(org.apache.maven.model.Plugin plugin) {
    this.plugin = plugin;
  }

  public Plugin() {
    this.plugin = new org.apache.maven.model.Plugin();
  }

  /**
   * Retrieves the plugin model represented by a maven core object. Meant to be used just by the classes in the package.
   *
   * @return a {@link org.apache.maven.model.Plugin}
   */
  protected org.apache.maven.model.Plugin getInnerModel() {
    return plugin;
  }

  /**
   * Retrieves the plugin artifact id.
   *
   * @return a {@link String}
   */
  public String getArtifactId() {
    return plugin.getArtifactId();
  }

  public void setArtifactId(String artifactId) {
    plugin.setArtifactId(artifactId);
  }

  /**
   * Retrieves the plugin group id.
   *
   * @return a {@link String}
   */
  public String getGroupId() {
    return plugin.getGroupId();
  }

  /**
   * Sets the plugin group id.
   *
   * @param groupId
   */
  public void setGroupId(String groupId) {
    plugin.setGroupId(groupId);
  }

  /**
   * Retrieves the plugin version or valid range of versions.
   *
   * @return a {@link String}
   */
  public String getVersion() {
    return plugin.getVersion();
  }

  /**
   * Sets the plugin version or valid range of versions.
   *
   * @param version
   */
  public void setVersion(String version) {
    plugin.setVersion(version);
  }

  /**
   * Retrieves the plugin dependencies.
   *
   * @return a {@link List<Plugin>}
   */
  public List<Dependency> getDependencies() {
    return plugin.getDependencies().stream().map(Dependency::new).collect(toList());
  }

  /**
   * Sets the plugin dependencies.
   *
   * @param dependencies
   */
  public void setDependencies(List<Dependency> dependencies) {
    plugin.setDependencies(dependencies.stream().map(Dependency::getInnerModel).collect(toList()));
  }

  /**
   * Retrieves the plugin executions.
   *
   * @return a {@link List<PluginExecution>}
   */
  public List<PluginExecution> getExecutions() {
    return plugin.getExecutions().stream().map(PluginExecution::new).collect(toList());
  }

  /**
   * Sets the plugin executions. The list of executions should have unique ids.
   *
   * @param executions
   * @return true if the list was successfully set, false otherwise
   */
  public boolean setExecutions(List<PluginExecution> executions) {
    if (areAllExecutionIdsUnique(executions)) {
      plugin.setExecutions(executions.stream().map(PluginExecution::getInnerModel).collect(toList()));
      return true;
    }
    return false;
  }

  /**
   * Retrieves the plugin extensions field. Even though it is represented by a string, it represents either true or false.
   *
   * @return a {@link String}
   */
  public String getExtensions() {
    return plugin.getExtensions();
  }

  /**
   * Sets the plugin extensions field. The parameter should be a string representing either true or false.
   *
   * @param extensions
   */
  public void setExtensions(String extensions) {
    if (StringUtils.equals("true", extensions) || StringUtils.equals("false", extensions)) {
      plugin.setExtensions(extensions);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Plugin that = (Plugin) o;
    return Objects.equals(plugin, that.plugin);
  }

  @Override
  public int hashCode() {
    return Objects.hash(plugin);
  }

  public Xpp3Dom getConfiguration() {
    Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
    if (configuration == null) {
      configuration = new Xpp3Dom("configuration");
      plugin.setConfiguration(configuration);
    }
    return configuration;
  }

  /**
   * A builder of plugins.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class PluginBuilder {

    private String artifactId;
    private String groupId;
    private String version;
    private List<Dependency> dependencies;
    private List<PluginExecution> executions;
    private String extensions;

    public PluginBuilder withArtifactId(String artifactId) {
      this.artifactId = artifactId;
      return this;
    }

    public PluginBuilder withGroupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public PluginBuilder withVersion(String version) {
      this.version = version;
      return this;
    }

    public PluginBuilder withDependencies(List<Dependency> dependencies) {
      this.dependencies = dependencies;
      return this;
    }

    public PluginBuilder withExecutions(List<PluginExecution> executions) {
      this.executions = executions;
      return this;
    }

    public PluginBuilder withExtensions(String extensions) {
      this.extensions = extensions;
      return this;
    }

    /**
     * Builds the plugin. Artifact id, group id and version are mandatory fields. Also, when declared, dependencies and extensions should not be empty strings, and executions should have unique ids.
     *
     * @return a plugin instance
     */
    public Plugin build() {
      checkArgument(isNotBlank(artifactId), "Artifact id cannot be null nor empty");
      checkArgument(isNotBlank(groupId), "Group id cannot be null nor empty");
      checkArgument(isNotBlank(version), "Version cannot be null nor empty");

      Plugin plugin = new Plugin();
      plugin.setArtifactId(artifactId);
      plugin.setGroupId(groupId);
      plugin.setVersion(version);

      if (dependencies != null) {
        plugin.setDependencies(dependencies);
      }

      if (executions != null) {
        checkArgument(areAllExecutionIdsUnique(executions), "Execution ids should be unique");
        plugin.setExecutions(executions);
      }

      if (extensions != null) {
        plugin.setExtensions(extensions);
      }

      return plugin;
    }

    protected static boolean areAllExecutionIdsUnique(List<PluginExecution> executions) {
      return executions.size() == executions.stream().map(PluginExecution::getId).collect(toSet()).size();
    }
  }
}
