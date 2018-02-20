/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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

public class MigrationJobDBTest {

  private MigrationJob migrationJob;
  private Document docRestoreFile;
  private String USE_CASE_FILE_PATH = "src/test/resources/mule/examples/db/db-use-case.xml";
  private String TASKS_FILE_PATH = "src/test/resources/mule/tasks/db/db-config-rules.json";

  @Before
  public void setUp() throws Exception {
    ArrayList<String> filePath1 = new ArrayList<String>();
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
    migrationJob.setConfigFilePath(TASKS_FILE_PATH);
    migrationJob.execute();
  }

  @After
  public void restoreFileState() throws Exception {
    restoreTestDocument(docRestoreFile, USE_CASE_FILE_PATH);
  }
}
