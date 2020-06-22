/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.pom;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;
import java.util.Objects;

/**
 * Represents a plugin in the pom model. The default id is "default".
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PluginExecution {

  protected org.apache.maven.model.PluginExecution pluginExecution;

  protected PluginExecution(org.apache.maven.model.PluginExecution pluginExecution) {
    this.pluginExecution = pluginExecution;
  }

  private PluginExecution() {
    this.pluginExecution = new org.apache.maven.model.PluginExecution();
  }

  /**
   * Retrieves the plugin execution model represented by a maven core object. Meant to be used just by the classes in the package.
   *
   * @return the plugin execution inner model
   */
  protected org.apache.maven.model.PluginExecution getInnerModel() {
    return pluginExecution;
  }

  /**
   * The default plugin execution id
   */
  public static final String DEFAULT_EXECUTION_ID = org.apache.maven.model.PluginExecution.DEFAULT_EXECUTION_ID;

  /**
   * Adds a goal to the plugin execution
   *
   * @param string
   */
  public void addGoal(String string) {
    pluginExecution.addGoal(string);
  }

  /**
   * Retrieves all the goals defined to the plugin execution
   *
   * @return a {@link List<String>} representing the goals
   */
  public List<String> getGoals() {
    return pluginExecution.getGoals();
  }

  /**
   * Retrieves the configuration tag of this execution
   *
   * @return a {@link Xpp3Dom} representing the configuration
   */
  public Xpp3Dom getConfiguration() {
    return (Xpp3Dom) pluginExecution.getConfiguration();
  }

  /**
   * Retrieves the plugin execution id
   *
   * @return a string representation of the id
   */
  public String getId() {
    return pluginExecution.getId();
  }

  /**
   * Retrieves the phase defined in the plugin execution
   *
   * @return a string representation of the phase
   */
  public String getPhase() {
    return pluginExecution.getPhase();
  }

  /**
   * Retrieves the priority of the plugin execution
   *
   * @return an integer representation of the priority
   */
  public int getPriority() {
    return pluginExecution.getPriority();
  }

  /**
   * Removes a goal of the plugin execution
   *
   * @param string a string representation of the goal to be removed
   */
  public void removeGoal(String string) {
    pluginExecution.removeGoal(string);
  }

  /**
   * Sets a {@link List<String>} of goals to the plugin execution
   *
   * @param goals a {@link List<String>} representing the goals
   */
  public void setGoals(List<String> goals) {
    pluginExecution.setGoals(goals);
  }

  /**
   * Sets a {@link Xpp3Dom} representing the configuration
   *
   * @param configuration a {@link Xpp3Dom} element representation of the configuration
   */
  public void setConfiguration(Xpp3Dom configuration) {
    pluginExecution.setConfiguration(configuration);
  }

  /**
   * Sets the plugin execution id
   *
   * @param id a string representation of the id
   */
  public void setId(String id) {
    pluginExecution.setId(id);
  }

  /**
   * Sets the phase in the plugin execution
   *
   * @param phase a string representation of the phase
   */
  public void setPhase(String phase) {
    pluginExecution.setPhase(phase);
  }

  /**
   * Sets the priority of the plugin execution
   *
   * @param priority an integer representation of the priority
   */
  public void setPriority(int priority) {
    pluginExecution.setPriority(priority);
  }

  /**
   * toString implementation of the plugin execution
   *
   * @return a string with the plugin execution id
   */
  @Override
  public String toString() {
    return pluginExecution.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PluginExecution that = (PluginExecution) o;

    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  /**
   * A builder of plugin executions.
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class PluginExecutionBuilder {

    private List<String> goals;
    private String id;
    private String phase;
    private Integer priority;

    /**
     * Sets the list of goals in the plugin execution id to be built.
     *
     * @param goals a {@link List<String>} representing the goals
     * @return the builder
     */
    public PluginExecutionBuilder withGoals(List<String> goals) {
      this.goals = goals;
      return this;
    }

    /**
     * Sets the id of the plugin execution to be built.
     *
     * @param id a string representation of the id
     * @return the builder
     */
    public PluginExecutionBuilder withId(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the phase defined in the plugin execution to be built.
     *
     * @param phase a string representation of the phase
     * @return the builder
     */
    public PluginExecutionBuilder withPhase(String phase) {
      this.phase = phase;
      return this;
    }

    /**
     * Sets the priority of the plugin execution to be built.
     *
     * @param priority an integer representation of the priority
     * @return the builder
     */
    public PluginExecutionBuilder withPriority(Integer priority) {
      this.priority = priority;
      return this;
    }

    /**
     * Build the {@link PluginExecution}.
     *
     * @return an instance of the {@link PluginExecution} with the values provided to the builder instance
     * @throws IllegalArgumentException if either id or phase are blank
     */
    public PluginExecution build() {
      PluginExecution pluginExecution = new PluginExecution();

      if (goals != null) {
        pluginExecution.setGoals(goals);
      }

      if (id != null) {
        checkArgument(isNotBlank(id), "Id should not be blank");
        pluginExecution.setId(id);
      }

      if (phase != null) {
        checkArgument(isNotBlank(phase), "Phase should not be blank");
        pluginExecution.setPhase(phase);
      }

      if (priority != null) {
        pluginExecution.setId(priority.toString());
      }

      return pluginExecution;
    }
  }
}
