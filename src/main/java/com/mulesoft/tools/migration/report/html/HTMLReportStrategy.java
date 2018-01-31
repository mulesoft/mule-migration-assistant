/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html;

import com.google.gson.Gson;
import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.html.execution.*;
import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.step.MigrationStep;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by julianpascual on 7/21/17.
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
                taskReport = new TaskExecutionStatus(data.getTask().getTaskDescriptor(), "Applied");
                fileReport.addTaskApplied(taskReport);
            } else if (data.getReportCategory().equals(ReportCategory.RULE_APPLIED) | data.getReportCategory().equals(ReportCategory.ERROR)) {
                taskReport.addStepApplied(data.getStep(), data.getReportCategory().getDescription());
                if (data.getReportCategory().equals(ReportCategory.ERROR)) {
                    taskReport.setTaskStatus(ReportCategory.ERROR.getDescription());
                }
            }
        }

//        TODO: Define later how to show this information into the HTML
    }
}
