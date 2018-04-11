/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * @author Mulesoft Inc.
 */
public class MigrationJobBuilderTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";

  private MigrationJob migrationJob;
  private List<AbstractMigrationTask> tasks;
  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    tasks = mock(List.class);
    originalProjectPath = temporaryFolder.newFolder(ORIGINAL_PROJECT_NAME).toPath();
    migratedProjectPath = temporaryFolder.newFolder(MIGRATED_PROJECT_NAME).toPath();
  }

  @Test(expected = IllegalStateException.class)
  public void setProjectNull() {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setOutputProjectNull() {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withOutputProject(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setMigrationTasksNull() {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(null).build();
  }

  @Test
  public void buildMigrationJob() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(tasks)
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .build();
  }
}
