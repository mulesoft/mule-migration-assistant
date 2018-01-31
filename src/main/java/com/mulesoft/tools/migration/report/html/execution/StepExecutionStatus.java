/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
