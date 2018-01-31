/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html.execution;

import com.mulesoft.tools.migration.task.step.MigrationStep;

import java.util.ArrayList;

/**
 * Created by julianpascual on 7/21/17.
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
        StepExecutionStatus stepStatus = new StepExecutionStatus(step.getStepDescriptor(), status);
        stepsApplied.add(stepStatus);
    }
}
