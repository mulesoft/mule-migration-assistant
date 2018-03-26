/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.engine.step.MigrationStep;
import com.mulesoft.tools.migration.engine.task.DefaultMigrationTask;
import com.mulesoft.tools.migration.engine.task.MigrationTask;
import com.mulesoft.tools.migration.engine.task.Version;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.structure.ProjectType;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author Mulesoft Inc.
 */
public class MigrationJobTest {

  private static final String ORIGINAL_PROJECT_NAME = "original-project";
  private static final String MIGRATED_PROJECT_NAME = "migrated-project";
  private static final String MUNIT_SECTIONS_SAMPLE_XML = "munit-sections-sample.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SECTIONS_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SECTIONS_SAMPLE_XML);

  private MigrationJob migrationJob;
  private MigrationTask migrationTask;
  private List<DefaultMigrationTask> tasks;
  private ApplicationModel applicationModelMock;
  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    migrationTask = new MigrationJobTest.MigrationTaskImpl();
    tasks = new ArrayList<>();
    tasks.add((DefaultMigrationTask) migrationTask);

    buildOriginalProject();
    migratedProjectPath = temporaryFolder.newFolder(MIGRATED_PROJECT_NAME).toPath();
  }

  private void buildOriginalProject() throws IOException {
    originalProjectPath = temporaryFolder.newFolder(ORIGINAL_PROJECT_NAME).toPath();

    File app = originalProjectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(MUNIT_SECTIONS_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MUNIT_SECTIONS_SAMPLE_PATH.getFileName().toString()));
  }

  @Test
  public void executeWithNullSteps() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(tasks)
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .build();

    migrationJob.execute();
  }

  @Test
  public void executeWithTaskThatFailsNotStopExecution() throws Exception {
    migrationTask = mock(DefaultMigrationTask.class);
    doThrow(MigrationTaskException.class)
        .when(migrationTask)
        .execute();

    tasks.add((DefaultMigrationTask) migrationTask);
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(tasks)
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .build();
    migrationJob.execute();
    verify(migrationTask, times(1)).execute();
  }

  private static final class MigrationTaskImpl extends DefaultMigrationTask {

    private Set<MigrationStep> migrationSteps;

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Set<MigrationStep> getSteps() {
      return this.migrationSteps;
    }

    public void setMigrationSteps(Set<MigrationStep> migrationSteps) {
      this.migrationSteps = migrationSteps;
    }


    @Override
    public Version getTo() {
      return null;
    }

    @Override
    public Version getFrom() {
      return null;
    }

    @Override
    public ProjectType getProjectType() {
      return null;
    }
  }
}
