/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html;

import java.util.ArrayList;

import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.html.model.ExecutionData;
import com.mulesoft.tools.migration.report.html.model.FileExecutionStatus;
import com.mulesoft.tools.migration.report.html.model.JobExecutionStatus;
import com.mulesoft.tools.migration.report.html.model.TaskExecutionStatus;
import com.mulesoft.tools.migration.engine.MigrationTask;
import com.mulesoft.tools.migration.engine.MigrationStep;

/**
 * It knows how to report data which will be printed in HTML format
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HTMLReportStrategy implements ReportingStrategy {

  private ArrayList<ExecutionData> collectedData = new ArrayList<>();

  @Override
  public void log(String message, ReportCategory reportCategory, String filePath, MigrationTask task, MigrationStep step) {
    ExecutionData data = new ExecutionData(message, reportCategory, filePath, task, step);
    collectedData.add(data);
  }

  public void generateReport() {

    JobExecutionStatus jobReport = new JobExecutionStatus();
    FileExecutionStatus fileReport = null;
    TaskExecutionStatus taskReport = null;

    for (ExecutionData data : collectedData) {
      if (data.getReportCategory().equals(ReportCategory.WORKING_WITH_FILE)) {
        fileReport = new FileExecutionStatus(data.getFilePath());
        jobReport.addFileMigrationStatus(fileReport);
      } else if (data.getReportCategory().equals(ReportCategory.WORKING_WITH_NODES)) {
        taskReport = new TaskExecutionStatus(data.getTask().getDescription(), "Applied");
        fileReport.addTaskApplied(taskReport);
      } else if (data.getReportCategory().equals(ReportCategory.RULE_APPLIED)
          | data.getReportCategory().equals(ReportCategory.ERROR)) {
        taskReport.addStepApplied(data.getStep(), data.getReportCategory().getDescription());
        if (data.getReportCategory().equals(ReportCategory.ERROR)) {
          taskReport.setTaskStatus(ReportCategory.ERROR.getDescription());
        }
      }
    }

    // TODO: Define later how to show this information into the HTML
  }
}
