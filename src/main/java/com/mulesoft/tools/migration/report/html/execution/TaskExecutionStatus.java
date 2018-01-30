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
