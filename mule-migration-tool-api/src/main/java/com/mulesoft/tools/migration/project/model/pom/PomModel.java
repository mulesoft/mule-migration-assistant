/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.pom;

import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.buildMinimalMule4ApplicationPom;
import static java.util.stream.Collectors.toList;

import org.apache.maven.model.Build;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

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
   * @return a {@link List<Dependency>}
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
    List dependencies = model.getDependencies().stream()
        .filter(dep -> dep.getGroupId().equals(dependency.getGroupId()) && dep.getArtifactId().equals(dependency.getArtifactId()))
        .collect(toList());
    List<org.apache.maven.model.Dependency> newDeps = new ArrayList<>(model.getDependencies());
    newDeps.removeAll(dependencies);
    model.setDependencies(newDeps);
    return model.getDependencies().size() == originalNumDependencies - 1;
  }

  /**
   * Retrieves the packaging type declared in the pom.
   *
   * @return a {@link String}
   */
  public String getPackaging() {
    return model.getPackaging();
  }

  /**
   * Retrieves a deep copy of the maven model.
   *
   * @return a {@link Model}
   */
  public Model getMavenModelCopy() {
    return model.clone();
  }

  /**
   * Sets the packaging type in the pom.
   *
   * @param packaging
   */
  public void setPackaging(String packaging) {
    model.setPackaging(packaging);
  }

  /**
   * Retrieves the description in the pom.
   *
   * @return a {@link String}
   */
  public String getDescription() {
    return model.getDescription();
  }

  /**
   * Sets the description in the pom.
   *
   * @param description
   */
  public void setDescription(String description) {
    model.setDescription(description);
  }

  /**
   * Retrieves the artifact id declared in the pom.
   *
   * @return a {@link String}
   */
  public String getArtifactId() {
    return model.getArtifactId();
  }

  /**
   * Sets the artifact id in the pom.
   *
   * @param artifactId
   */
  public void setArtifactId(String artifactId) {
    model.setArtifactId(artifactId);
  }

  /**
   * Retrieves the group id declared in the pom.
   *
   * @return a {@link String}
   */
  public String getGroupId() {
    return model.getGroupId();
  }

  /**
   * Sets the group id in the pom.
   *
   * @param groupId
   */
  public void setGroupId(String groupId) {
    model.setGroupId(groupId);
  }

  /**
   * Retrieves the version declared in the pom.
   *
   * @return a {@link String}
   */
  public String getVersion() {
    return model.getVersion();
  }

  /**
   * Sets the version in the pom.
   *
   * @param version
   */
  public void setVersion(String version) {
    model.setVersion(version);
  }

  /**
   * Retrieves the name declared in the pom.
   *
   * @return a {@link String}
   */
  public String getName() {
    return model.getName();
  }

  /**
   * Sets the name in the pom.
   *
   * @param name
   */
  public void setName(String name) {
    model.setName(name);
  }

  /**
   * Retrieves the properties declared in the pom.
   *
   * @return a {@link Properties}
   */
  public Properties getProperties() {
    return model.getProperties();
  }

  /**
   * Sets the properties in the pom.
   *
   * @param properties
   */
  public void setProperties(Properties properties) {
    model.setProperties(properties);
  }

  /**
   * Sets a key-valued pair in the properties map.
   *
   * @param key
   * @param value
   */
  public void addProperty(String key, String value) {
    model.addProperty(key, value);
  }

  /**
   * Removes a property from the pom model properties.
   *
   * @param propertyName
   * @throws NullPointerException if the key is <code>null</code>
   */
  public void removeProperty(String propertyName) {
    model.getProperties().remove(propertyName);
  }

  /**
   * Sets the pom model version.
   *
   * @param modelVersion
   */
  public void setModelVersion(String modelVersion) {
    model.setModelVersion(modelVersion);
  }

  /**
   * Retrieves the pom model version.
   *
   * @return a {@link String}
   */
  public String getModelVersion() {
    return model.getModelVersion();
  }

  /**
   * Adds a plugin to the build section in the pom.
   *
   * @param plugin
   */
  public void addPlugin(Plugin plugin) {
    getBuild().addPlugin(plugin.getInnerModel());
  }

  /**
   * Retrieves the list of plugins declared in the build section.
   *
   * @return a {@link List<Plugin>}
   */
  public List<Plugin> getPlugins() {
    return getBuild().getPlugins().stream().map(Plugin::new).collect(toList());
  }

  /**
   * Removes the specified plugin from the list of plugins declared in the build section.
   *
   * @param plugin
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
   * @return an optional of the removed plugin
   */
  public Optional<Plugin> removePlugin(Predicate<Plugin> pluginPredicate) {
    Optional<Plugin> removedPlugin = getPlugins().stream().filter(pluginPredicate).findFirst();
    removedPlugin.ifPresent(this::removePlugin);
    return removedPlugin;
  }

  /**
   * Sets distribution management
   *
   * @param distributionManagement
   */
  public void setDistributionManagement(DistributionManagement distributionManagement) {
    model.setDistributionManagement(distributionManagement);
  }

  /**
   * Retrieves distribution management
   *
   * @return a {@link DistributionManagement}
   */
  public DistributionManagement getDistributionManagement() {
    return model.getDistributionManagement();
  }

  public List<Profile> getProfiles() {
    return model.getProfiles();
  }

  /**
   * Retrieves the build section.
   *
   * @return a {@link Build}
   */
  private Build getBuild() {
    if (model.getBuild() == null) {
      model.setBuild(new Build());
    }
    return model.getBuild();
  }

  /**
   * Retrieves the list of reapositories repository section.
   */
  public List<Repository> getRepositories() {
    return model.getRepositories().stream().map(r -> Repository.of(r)).collect(toList());
  }

  /**
   * Adds a {@link Repository} to the {@link PomModel} in the repository section.
   *
   * @param repository
   */
  public void addRepository(Repository repository) {
    model.addRepository(repository.getInnerModel());
  }

  /**
   * Retrieves the list of reapositories plugin repository section.
   */
  public List<Repository> getPluginRepositories() {
    return model.getPluginRepositories().stream().map(r -> Repository.of(r)).collect(toList());
  }

  /**
   * Adds a {@link Repository} to the {@link PomModel} in the plugin repository section.
   *
   * @param repository
   */
  public void addPluginRepository(Repository repository) {
    model.addPluginRepository(repository.getInnerModel());
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
    private String artifactId = "migrated-project";
    private String packaging = "mule-application";

    public PomModelBuilder withPom(Path pomPath) {
      this.pomPath = pomPath;
      return this;
    }

    public PomModelBuilder withArtifactId(String artifactId) {
      this.artifactId = artifactId;
      return this;
    }

    public PomModelBuilder withPackaging(String packaging) {
      this.packaging = packaging;
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
      if (pomPath == null || !pomPath.toFile().exists()) {
        // TODO: NEED TO DEFINE HOW TO COME UP WITH THE POM GAV COORDINATES
        return buildMinimalMule4ApplicationPom("org.mule.migrated", artifactId, "1.0.0-SNAPSHOT", packaging);
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
