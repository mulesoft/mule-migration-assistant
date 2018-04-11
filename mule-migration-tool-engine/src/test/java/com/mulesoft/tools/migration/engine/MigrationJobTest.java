/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.Version;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
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

import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static java.nio.charset.StandardCharsets.UTF_8;
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
  private static final String MULE_SAMPLE_XML = "mule-sample.xml";
  private static final Path MULE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path MULE_SAMPLE_PATH = MULE_EXAMPLES_PATH.resolve(MULE_SAMPLE_XML);

  private MigrationJob migrationJob;
  private MigrationTask migrationTask;
  private List<AbstractMigrationTask> tasks;
  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    migrationTask = new MigrationJobTest.MigrationTaskImpl();
    tasks = new ArrayList<>();
    tasks.add((AbstractMigrationTask) migrationTask);

    buildOriginalProject();
    migratedProjectPath = temporaryFolder.newFolder(MIGRATED_PROJECT_NAME).toPath();
  }

  private void buildOriginalProject() throws IOException {
    originalProjectPath = temporaryFolder.newFolder(ORIGINAL_PROJECT_NAME).toPath();

    File app = originalProjectPath.resolve("src").resolve("test").resolve("munit").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(MUNIT_SECTIONS_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MUNIT_SECTIONS_SAMPLE_PATH.getFileName().toString()));

    app = originalProjectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    sample = this.getClass().getClassLoader().getResource(MULE_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MULE_SAMPLE_PATH.getFileName().toString()));

    FileUtils.write(new File(originalProjectPath.toFile(), "pom.xml"), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
        "  <modelVersion>4.0.0</modelVersion>\n" +
        "  <groupId>groupId</groupId>\n" +
        "  <artifactId>artifactid</artifactId>\n" +
        "  <version>1.0-SNAPSHOT</version>\n" +
        "  <packaging>jar</packaging>\n" +
        "  <name>projectName</name>\n" +
        "  \n" +
        "  <properties>\n" +
        "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
        "  </properties>\n" +
        "  \n" +
        "  <build>\n" +
        "    <plugins>\n" +
        "      <plugin>\n" +
        "        <groupId>org.apache.maven.plugins</groupId>\n" +
        "        <artifactId>maven-compiler-plugin</artifactId>\n" +
        "        <version>2.5.1</version>\n" +
        "        <configuration>\n" +
        "          <source>1.6</source>\n" +
        "          <target>1.6</target>\n" +
        "        </configuration>\n" +
        "      </plugin>\n" +
        "    </plugins>\n" +
        "  </build>\n" +
        "</project>", UTF_8);
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
  public void execute() throws Exception {
    tasks = new ArrayList<>();
    migrationTask = new MunitMigrationTask();
    tasks.add((AbstractMigrationTask) migrationTask);

    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(tasks)
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .build();

    migrationJob.execute();
  }

  @Test
  public void executeWithTaskLocator() throws Exception {
    MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(MULE_3_VERSION, MULE_4_VERSION, MULE_FOUR_APPLICATION);
    tasks = migrationTaskLocator.locate();

    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(tasks)
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .build();

    migrationJob.execute();
  }

  @Test
  public void executeWithTaskThatFailsNotStopExecution() throws Exception {
    migrationTask = mock(AbstractMigrationTask.class);
    doThrow(MigrationTaskException.class)
        .when(migrationTask)
        .execute();

    tasks.add((AbstractMigrationTask) migrationTask);
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withMigrationTasks(tasks)
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .build();
    migrationJob.execute();
    verify(migrationTask, times(1)).execute();
  }

  private static final class MigrationTaskImpl extends AbstractMigrationTask {

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
