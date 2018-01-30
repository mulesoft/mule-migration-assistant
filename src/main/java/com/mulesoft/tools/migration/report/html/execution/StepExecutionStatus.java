package com.mulesoft.tools.migration.report.html.execution;

/**
 * Created by julianpascual on 7/21/17.
 */
public class StepExecutionStatus {

    private String stepDescriptor;
    private String stepStatus;

    public StepExecutionStatus(String stepDescriptor, String status) {
        this.stepDescriptor = stepDescriptor;
        this.stepStatus = status;
    }

    public String getStepDescriptor() {
        return stepDescriptor;
    }

    public void setStepDescriptor(String stepDescriptor) {
        this.stepDescriptor = stepDescriptor;
    }

    public String getStepStatus() {
        return stepStatus;
    }

    public void setStepStatus(String status) {
        this.stepStatus = status;
    }
}
