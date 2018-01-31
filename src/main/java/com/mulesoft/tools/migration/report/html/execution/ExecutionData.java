/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html.execution;

import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.step.MigrationStep;

/**
 * Created by julianpascual on 7/25/17.
 */
public class ExecutionData {

    private String message;
    private ReportCategory reportCategory;
    private String filePath;
    private MigrationTask task;
    private MigrationStep step;

    public ExecutionData(String message, ReportCategory reportCategory, String filePath, MigrationTask task, MigrationStep step) {
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

    public MigrationTask getTask() {
        return task;
    }

    public MigrationStep getStep() {
        return step;
    }
}
