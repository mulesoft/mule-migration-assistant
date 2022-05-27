/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.model.pom.PomModel.DEFAULT_GROUP_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModel.DEFAULT_VERSION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.exception.MigrationTaskException;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
import com.mulesoft.tools.migration.report.DefaultMigrationReport;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.reflect.Whitebox;

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

  public static final String MULE_413_VERSION = "4.1.3";
  public static final String MULE_380_VERSION = "3.8.0";
  public static final String MULE_370_VERSION = "3.7.0";

  private final List<AbstractMigrationTask> migrationTasks = new ArrayList<>();
  private MigrationJob migrationJob;
  private Path originalProjectPath;
  private Path migratedProjectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    originalProjectPath = buildOriginalProject(ORIGINAL_PROJECT_NAME, true);
    migratedProjectPath = temporaryFolder.getRoot().toPath().resolve(MIGRATED_PROJECT_NAME).toAbsolutePath();
  }

  private Path buildOriginalProject(String projectName, boolean withPom) throws IOException {
    Path projectPath = temporaryFolder.newFolder(projectName).toPath();

    File app = projectPath.resolve("src").resolve("test").resolve("munit").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(MUNIT_SECTIONS_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MUNIT_SECTIONS_SAMPLE_PATH.getFileName().toString()));

    app = projectPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    sample = this.getClass().getClassLoader().getResource(MULE_SAMPLE_PATH.toString());
    FileUtils.copyURLToFile(sample, new File(app, MULE_SAMPLE_PATH.getFileName().toString()));

    if (withPom) {
      FileUtils.write(new File(projectPath.toFile(), "pom.xml"), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
    return projectPath;
  }

  @Test
  public void executeWithTaskThatFailsNotStopExecution() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_380_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();

    AbstractMigrationTask migrationTask1 = mock(AbstractMigrationTask.class);
    doThrow(MigrationTaskException.class)
        .when(migrationTask1)
        .execute(any(MigrationReport.class));
    when(migrationTask1.getApplicableProjectTypes()).thenReturn(singleton(MULE_FOUR_APPLICATION));

    AbstractMigrationTask migrationTask2 = mock(AbstractMigrationTask.class);
    when(migrationTask2.getApplicableProjectTypes()).thenReturn(singleton(MULE_FOUR_APPLICATION));

    migrationTasks.add(migrationTask1);
    migrationTasks.add(migrationTask2);
    Whitebox.setInternalState(migrationJob, "migrationTasks", migrationTasks);
    try {
      migrationJob.execute(new DefaultMigrationReport());
      fail("expected MigrationTaskException");
    } catch (MigrationTaskException mte) {
      verify(migrationTask1, times(1)).execute(any(MigrationReport.class));
      verify(migrationTask2, times(1)).execute(any(MigrationReport.class));
    } catch (Exception e) {
      fail("expected MigrationTaskException");
    }
  }

  @Test(expected = MigrationTaskException.class)
  public void executeWithTaskThatFailsAndStopExecution() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_380_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .withCancelOnError(true)
        .build();

    AbstractMigrationTask migrationTask = mock(AbstractMigrationTask.class);
    doThrow(MigrationTaskException.class)
        .when(migrationTask)
        .execute(any(MigrationReport.class));
    when(migrationTask.getApplicableProjectTypes()).thenReturn(singleton(MULE_FOUR_APPLICATION));

    migrationTasks.add(migrationTask);
    Whitebox.setInternalState(migrationJob, "migrationTasks", migrationTasks);
    migrationJob.execute(new DefaultMigrationReport());
  }

  @Test
  public void executeWithEmptyTaskList() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_370_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();
    migrationJob.execute(new DefaultMigrationReport());
  }

  @Test
  public void execute() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_380_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();

    migrationJob.execute(new DefaultMigrationReport());
  }

  @Test
  public void executeCheckApplicationModel() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_380_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();

    MunitMigrationTask migrationTask = new MunitMigrationTask();
    migrationTasks.add(migrationTask);
    Whitebox.setInternalState(migrationJob, "migrationTasks", migrationTasks);
    migrationJob.execute(new DefaultMigrationReport());

    assertThat("The application model generated is wrong.", migrationTask.getApplicationModel().getApplicationDocuments().size(),
               is(2));

  }

  @Test
  public void shouldNotOverrideArtifactIdWhenPomExists() throws Exception {
    assertProjectGav(originalProjectPath, ":customArtifactId:", "groupId", "artifactid", "1.0-SNAPSHOT");
  }

  @Test
  public void shouldOverrideOnlyArtifactIdWhenPomDoesNotExist() throws Exception {
    Path projectPath = buildOriginalProject("pomLess", false);
    assertProjectGav(projectPath, ":customArtifactId:", DEFAULT_GROUP_ID, "customArtifactId", DEFAULT_VERSION);
  }

  @Test
  public void shouldOverrideFullGavWhenPomDoesNotExist() throws Exception {
    Path projectPath = buildOriginalProject("pomLess", false);
    assertProjectGav(projectPath, "custom.group.id:customArtifactId:1.2.3", "custom.group.id", "customArtifactId", "1.2.3");
  }

  private void assertProjectGav(Path projectPath, String gav, String expectedGroupId, String expectedArtifactId,
                                String expectedVersion)
      throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(projectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_380_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .withProjectGAV(gav)
        .build();

    MunitMigrationTask migrationTask = new MunitMigrationTask();
    migrationTasks.add(migrationTask);
    Whitebox.setInternalState(migrationJob, "migrationTasks", migrationTasks);
    migrationJob.execute(new DefaultMigrationReport());

    assertThat(migrationTask.getApplicationModel().getPomModel().get().getGroupId(), is(expectedGroupId));
    assertThat(migrationTask.getApplicationModel().getPomModel().get().getArtifactId(), is(expectedArtifactId));
    assertThat(migrationTask.getApplicationModel().getPomModel().get().getVersion(), is(expectedVersion));
  }

  @Test
  public void execute_assertGitIgnoreFile() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_370_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();
    migrationJob.execute(new DefaultMigrationReport());
    File gitIgnore = new File(migratedProjectPath.toFile(), ".gitignore");
    assertTrue(gitIgnore.exists());

  }

  @Test
  public void execute_assertContentGitIgnoreFile() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_370_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();
    migrationJob.execute(new DefaultMigrationReport());
    File gitIgnore = new File(migratedProjectPath.toFile(), ".gitignore");


    InputStream gitignoreResourceStream = getClass().getClassLoader().getResourceAsStream("gitignore-maven-template");
    byte[] sourceBytes = IOUtils.toByteArray(gitignoreResourceStream);
    byte[] targetBytes = FileUtils.readFileToByteArray(gitIgnore);
    assertArrayEquals(sourceBytes, targetBytes);
  }
}
