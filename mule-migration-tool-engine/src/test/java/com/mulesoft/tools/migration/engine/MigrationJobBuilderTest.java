/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

/**
 * @author Mulesoft Inc.
 */
public class MigrationJobBuilderTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";
  private static final String MULE4_VERSION = "4.1.2";

  private MigrationJob migrationJob;
  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    originalProjectPath = temporaryFolder.newFolder(ORIGINAL_PROJECT_NAME).toPath();
    migratedProjectPath = temporaryFolder.getRoot().toPath().resolve(MIGRATED_PROJECT_NAME).toAbsolutePath();
  }

  @Test(expected = IllegalStateException.class)
  public void setProjectNull() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setOutputProjectNull() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withOutputProject(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setOutputProjectTypeNull() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .build();
  }

  @Test(expected = IllegalStateException.class)
  public void setOutputVersionNull() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withOuputVersion(null).build();
  }

  @Test(expected = IllegalStateException.class)
  public void setInputVersionNull() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withInputVersion(null).build();
  }

  @Test
  public void buildMigrationJob() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_3_VERSION)
        .withOuputVersion(MULE4_VERSION)
        .build();
  }
}
