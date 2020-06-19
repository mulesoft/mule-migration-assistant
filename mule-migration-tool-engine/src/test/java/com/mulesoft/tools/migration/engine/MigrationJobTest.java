/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.engine;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    buildOriginalProject();
    migratedProjectPath = temporaryFolder.getRoot().toPath().resolve(MIGRATED_PROJECT_NAME).toAbsolutePath();
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
  public void executeWithTaskThatFailsNotStopExecution() throws Exception {
    migrationJob = new MigrationJob.MigrationJobBuilder()
        .withProject(originalProjectPath)
        .withOutputProject(migratedProjectPath)
        .withInputVersion(MULE_380_VERSION)
        .withOuputVersion(MULE_413_VERSION)
        .build();

    AbstractMigrationTask migrationTask = mock(AbstractMigrationTask.class);
    doThrow(MigrationTaskException.class)
        .when(migrationTask)
        .execute(any(MigrationReport.class));
    when(migrationTask.getApplicableProjectTypes()).thenReturn(singleton(MULE_FOUR_APPLICATION));

    migrationTasks.add(migrationTask);
    Whitebox.setInternalState(migrationJob, "migrationTasks", migrationTasks);
    migrationJob.execute(new DefaultMigrationReport());
    verify(migrationTask, times(1)).execute(any(MigrationReport.class));
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
}
