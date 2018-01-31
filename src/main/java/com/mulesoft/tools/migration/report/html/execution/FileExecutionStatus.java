/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html.execution;

import com.mulesoft.tools.migration.task.MigrationTask;

import java.util.ArrayList;

/**
 * Created by julianpascual on 7/21/17.
 */
public class FileExecutionStatus {

    private String fileName;
    private ArrayList<TaskExecutionStatus> tasksApplied = new ArrayList<>();

    public FileExecutionStatus(String fileName){
        this.setFileName(fileName);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addTaskApplied(TaskExecutionStatus taskApplied){
        tasksApplied.add(taskApplied);
    }
}
