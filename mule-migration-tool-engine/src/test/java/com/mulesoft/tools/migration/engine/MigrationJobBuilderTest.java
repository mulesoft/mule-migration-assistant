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

import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static org.mockito.Mockito.mock;

/**
 * @author Mulesoft Inc.
 */
public class MigrationJobBuilderTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";

  private MigrationJob migrationJob;
  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
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
  public void setOutputProjectTypeNull() {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withOutputProjectType(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setOutputVersionNull() {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withOuputVersion(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setInputVersionNull() {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withInputVersion(null).build();
  }

  @Test
  public void buildMigrationJob() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_3_VERSION)
        .withOuputVersion(MULE_4_VERSION)
        .withOutputProjectType(MULE_FOUR_APPLICATION)
        .build();
  }
}
