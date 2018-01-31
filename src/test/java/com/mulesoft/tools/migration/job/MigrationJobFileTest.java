/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.job;

import com.mulesoft.tools.migration.MigrationJob;
import org.jdom2.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.restoreTestDocument;

public class MigrationJobFileTest {
    private MigrationJob migrationJob;
    private Document docRestoreFile;
    private String USE_CASE_FILE_PATH = "src/test/resources/mule/examples/file/file-use-case.xml";
    private String TASKS_DIR_PATH = "src/test/resources/mule/tasks/file";

    @Before
    public void setUp() throws Exception {
        ArrayList<String> filePath1 = new ArrayList<>();
        filePath1.add(USE_CASE_FILE_PATH);

        migrationJob = new MigrationJob();
        migrationJob.setDocuments(filePath1);
        migrationJob.setBackUpProfile(Boolean.FALSE);
        docRestoreFile = getDocument(filePath1.get(0));
    }

    @Test
    public void jobWithTasksOnConfigFile() throws Exception {
        ArrayList<String> files = new ArrayList<String>(Arrays.asList(USE_CASE_FILE_PATH));

        migrationJob.setDocuments(files);
        migrationJob.setConfigFileDir(TASKS_DIR_PATH);
        migrationJob.execute();
    }

    @After
    public void restoreFileState() throws Exception {
        restoreTestDocument(docRestoreFile,USE_CASE_FILE_PATH);
    }
}
