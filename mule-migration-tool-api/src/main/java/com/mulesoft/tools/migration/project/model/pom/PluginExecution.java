/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.pom;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

  protected org.apache.maven.model.PluginExecution getInnerModel() {
    return pluginExecution;
  }

  public static final String DEFAULT_EXECUTION_ID = org.apache.maven.model.PluginExecution.DEFAULT_EXECUTION_ID;

  public void addGoal(String string) {
    pluginExecution.addGoal(string);
  }

  public List<String> getGoals() {
    return pluginExecution.getGoals();
  }

  public String getId() {
    return pluginExecution.getId();
  }

  public String getPhase() {
    return pluginExecution.getPhase();
  }

  public int getPriority() {
    return pluginExecution.getPriority();
  }

  public void removeGoal(String string) {
    pluginExecution.removeGoal(string);
  }

  public void setGoals(List<String> goals) {
    pluginExecution.setGoals(goals);
  }

  public void setId(String id) {
    pluginExecution.setId(id);
  }

  public void setPhase(String phase) {
    pluginExecution.setPhase(phase);
  }

  public void setPriority(int priority) {
    pluginExecution.setPriority(priority);
  }

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

    public PluginExecutionBuilder withGoals(List<String> goals) {
      this.goals = goals;
      return this;
    }

    public PluginExecutionBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public PluginExecutionBuilder withPhase(String phase) {
      this.phase = phase;
      return this;
    }

    public PluginExecutionBuilder withPriority(Integer priority) {
      this.priority = priority;
      return this;
    }

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
