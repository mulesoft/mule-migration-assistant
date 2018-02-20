/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

/**
 * Store data of a executed step
 * @author Mulesoft Inc.
 * @since 1.0.0
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
