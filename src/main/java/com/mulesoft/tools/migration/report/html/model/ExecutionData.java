/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.engine.task.DefaultMigrationTask;
import com.mulesoft.tools.migration.engine.step.DefaultMigrationStep;

/**
 * Store data of a reported event
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExecutionData {

  private ReportCategory reportCategory;
  private String filePath;
  private DefaultMigrationTask task;
  private DefaultMigrationStep step;
  private String message;

  public ExecutionData(String message, ReportCategory reportCategory, String filePath, DefaultMigrationTask task,
                       DefaultMigrationStep step) {
    this.message = message;
    this.reportCategory = reportCategory;
    this.filePath = filePath;
    this.task = task;
    this.step = step;
  }

  public String getMessage() {
    return message;
  }

  public ReportCategory getReportCategory() {
    return reportCategory;
  }

  public String getFilePath() {
    return filePath;
  }

  public DefaultMigrationTask getTask() {
    return task;
  }

  public DefaultMigrationStep getStep() {
    return step;
  }
}
