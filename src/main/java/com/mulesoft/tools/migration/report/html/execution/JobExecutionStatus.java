/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
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
