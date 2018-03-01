/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import java.util.ArrayList;

import com.mulesoft.tools.migration.engine.MigrationStep;

/**
 * Stores data of a executed task
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class TaskExecutionStatus {

  private String taskDescriptor;
  private String taskStatus;
  private ArrayList<StepExecutionStatus> stepsApplied = new ArrayList<>();

  public TaskExecutionStatus(String taskDescriptor, String taskStatus) {
    this.taskDescriptor = taskDescriptor;
    this.taskStatus = taskStatus;
  }

  public String getTaskDescriptor() {
    return taskDescriptor;
  }

  public void setTaskDescriptor(String taskDescriptor) {
    this.taskDescriptor = taskDescriptor;
  }

  public String getTaskStatus() {
    return taskStatus;
  }

  public void setTaskStatus(String taskStatus) {
    this.taskStatus = taskStatus;
  }

  public void addStepApplied(MigrationStep step, String status) {
    StepExecutionStatus stepStatus = new StepExecutionStatus(step.getDescription(), status);
    stepsApplied.add(stepStatus);
  }
}
