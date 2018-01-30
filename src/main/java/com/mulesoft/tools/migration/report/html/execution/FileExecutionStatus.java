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
