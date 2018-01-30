package com.mulesoft.tools.migration.report.html.execution;

import java.util.ArrayList;

/**
 * Created by julianpascual on 7/21/17.
 */
public class JobExecutionStatus {

    private ArrayList<FileExecutionStatus> filesMigrationStatus = new ArrayList<>();

    public void addFileMigrationStatus(FileExecutionStatus file) {
        filesMigrationStatus.add(file);
    }
}
