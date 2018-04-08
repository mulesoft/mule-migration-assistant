/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.pom.PomModelUtils.buildMinimalMule4ApplicationPom;
import static java.util.stream.Collectors.toList;

/**
 * The pom model.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PomModel {

  private final Model model;

  private PomModel(Model model) {
    this.model = model;
  }

  public PomModel() {
    this.model = new Model();
  }

  /**
   * Retrieves the list of dependencies in the pom model.
   *
   * @return the list of dependencies declared in the pom model
   */
  public List<Dependency> getDependencies() {
    return model.getDependencies().stream().map(Dependency::new).collect(toList());
  }

  /**
   * Sets the dependencies in the pom model.
   *
   * @param dependencies the list of the dependencies to be set in the pom model
   */
  public void setDependencies(List<Dependency> dependencies) {
    model.setDependencies(dependencies.stream().map(Dependency::getInnerModel).collect(toList()));
  }

  /**
   * Adds a dependency to the pom model dependencies list. The dependency is not added if another dependency with same artifact
   * id, group id and version is already present in the dependencies list
   *
   * @param dependency the dependencies to be added to the pom model
   * @return true if the dependency was added to the dependency list; false otherwise
   */
  public boolean addDependency(Dependency dependency) {
    if (dependency == null || model.getDependencies().stream().map(Dependency::new).anyMatch(dependency::equals)) {
      return false;
    }
    model.addDependency(dependency.getInnerModel());
    return true;
  }

  /**
   * Removes a dependency from the pom model dependencies list.
   *
   * @param dependency the dependency to be removed from the dependencies list
   * @return true if the dependency was removed from the dependency list; false otherwise
   */
  public boolean removeDependency(Dependency dependency) {
    int originalNumDependencies = model.getDependencies().size();
    List dependencies = model.getDependencies().stream().filter(dep -> dep.equals(dependency)).collect(toList());
    model.setDependencies(dependencies);
    return model.getDependencies().size() == originalNumDependencies - 1;
  }

  /**
   * Retrieves the packaging type declared in the pom.
   *
   * @return the packaging type
   */
  public String getPackaging() {
    return model.getPackaging();
  }

  /**
   * Retrieves a deep copy of the maven model.
   *
   * @return the model deep's copy
   */
  public Model getMavenModelCopy() {
    return model.clone();
  }

  /**
   * Sets the packaging type in the pom.
   *
   * @param packaging the packaging type to be set
   */
  public void setPackaging(String packaging) {
    model.setPackaging(packaging);
  }

  /**
   * Retrieves the artifact id declared in the pom.
   *
   * @return the artifact id declared in the pom
   */
  public String getArtifactId() {
    return model.getArtifactId();
  }

  /**
   * Sets the artifact id in the pom.
   *
   * @param artifactId the artifact id to be set
   */
  public void setArtifactId(String artifactId) {
    model.setArtifactId(artifactId);
  }

  /**
   * Retrieves the group id declared in the pom.
   *
   * @return the group id declared in the pom
   */
  public String getGroupId() {
    return model.getGroupId();
  }

  /**
   * Sets the group id in the pom.
   *
   * @param groupId the group id to be set
   */
  public void setGroupId(String groupId) {
    model.setGroupId(groupId);
  }

  /**
   * Retrieves the version declared in the pom.
   *
   * @return the version declared in the pom
   */
  public String getVersion() {
    return model.getVersion();
  }

  /**
   * Sets the version in the pom.
   *
   * @param version the version to be set
   */
  public void setVersion(String version) {
    model.setVersion(version);
  }

  /**
   * Retrieves the name declared in the pom.
   *
   * @return the name declared in the pom
   */
  public String getName() {
    return model.getName();
  }

  /**
   * Sets the name in the pom.
   *
   * @param name the name to be set
   */
  public void setName(String name) {
    model.setName(name);
  }

  /**
   * Retrieves the properties declared in the pom.
   *
   * @return the properties declared in the pom
   */
  public Properties getProperties() {
    return model.getProperties();
  }

  /**
   * Sets the properties in the pom.
   *
   * @param properties the properties to be set
   */
  public void setProperties(Properties properties) {
    model.setProperties(properties);
  }

  /**
   * Sets a key-valued pair in the properties map.
   *
   * @param key   the property key
   * @param value the property value
   */
  public void addProperty(String key, String value) {
    model.addProperty(key, value);
  }

  /**
   * Sets the pom model version.
   *
   * @param modelVersion the pom model version
   */
  public void setModelVersion(String modelVersion) {
    model.setModelVersion(modelVersion);
  }

  /**
   * Retrieves the pom model version.
   *
   * @return the pom model version
   */
  public String getModelVersion() {
    return model.getModelVersion();
  }

  /**
   * Adds a plugin to the build section in the pom.
   *
   * @param plugin the plugin to be added
   */
  public void addPlugin(Plugin plugin) {
    getBuild().addPlugin(plugin.getInnerModel());
  }

  /**
   * Retrieves the list of plugins declared in the build section.
   *
   * @return a list of plugins
   */
  public List<Plugin> getPlugins() {
    return getBuild().getPlugins().stream().map(Plugin::new).collect(toList());
  }

  /**
   * Removes the specified plugin from the list of plugins declared in the build section.
   *
   * @param plugin the plugin to be removed.
   */
  public void removePlugin(Plugin plugin) {
    getBuild().removePlugin(plugin.getInnerModel());
  }

  /**
   * Remove all the plugins that satisfies a predicate.
   *
   * @param pluginPredicate a predicate that evaluates to true if the plugin should be removed
   * @return the list of plugins removed
   */
  public List<Plugin> removePlugins(Predicate<Plugin> pluginPredicate) {
    List<Plugin> removedPlugins = getPlugins().stream().filter(pluginPredicate).collect(toList());
    removedPlugins.forEach(this::removePlugin);
    return removedPlugins;
  }

  /**
   * Remove the first plugins that satisfies a predicate.
   *
   * @param pluginPredicate a predicate that evaluates to true if the plugin should be removed
   * @return an optional containing the removed plugin, empty if no plugin satisifies the predicate
   */
  public Optional<Plugin> removePlugin(Predicate<Plugin> pluginPredicate) {
    Optional<Plugin> removedPlugin = getPlugins().stream().filter(pluginPredicate).findFirst();
    removedPlugin.ifPresent(this::removePlugin);
    return removedPlugin;
  }

  private Build getBuild() {
    if (model.getBuild() == null) {
      model.setBuild(new Build());
    }
    return model.getBuild();
  }

  /**
   * The pom model builder. It builds the pom model based on the pom location in the filesystem.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class PomModelBuilder {

    private final MavenXpp3Reader mavenReader = new MavenXpp3Reader();
    private Path pomPath;

    public PomModelBuilder withPom(Path pomPath) {
      this.pomPath = pomPath;
      return this;
    }

    /**
     * Builds the plugin based on the file pointed by the pom path. If such file does not exist, an empty model is returned.
     *
     * @return a pom model
     * @throws IOException
     * @throws XmlPullParserException
     */
    public PomModel build() throws IOException, XmlPullParserException {
      checkArgument(pomPath != null, "Pom path should not be null");
      if (!pomPath.toFile().exists()) {
        // TODO: NEED TO DEFINE HOW TO COME UP WITH THE POM GAV COORDINATES
        return buildMinimalMule4ApplicationPom("org.mule.migrated", "migrated-project", "1.0.0");
      }
      Model model = getModel(pomPath);
      return new PomModel(model);
    }

    private Model getModel(Path pomPath) throws IOException, XmlPullParserException {
      Model model;
      try (BufferedReader reader = new BufferedReader(new FileReader(pomPath.toFile()))) {
        model = mavenReader.read(reader);
      }

      return model;
    }

  }
}
