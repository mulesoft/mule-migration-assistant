/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.mulesoft.tools.migration.MigrationRunner;
import com.mulesoft.tools.migration.project.model.pom.PomModel.PomModelBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import junitx.framework.FileAssert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * Tests the whole migration process, starting with a Mule 3 source config, migrating it to Mule 4, and comparing the expected output files.
 */
public abstract class AbstractEndToEndTestCase {

  protected static final String ONLY_MIGRATE = getProperty("mule.test.migratorOnly");

  private static final String RUNTIME_VERSION = ofNullable(getProperty("mule.version")).orElse("4.3.0");

  @ClassRule
  public static TemporaryFolder mmaBinary = new TemporaryFolder();

  private static final Logger logger = LoggerFactory.getLogger(AbstractEndToEndTestCase.class);

  @Rule
  public TemporaryFolder migrationResult = new TemporaryFolder();


  public void simpleCase(String appName, String... additionalParams) throws Exception {
    String outputPath = migrate(appName, additionalParams);

    if (ONLY_MIGRATE != null) {
      return;
    }

    verifyOutput(outputPath, appName);
  }

  /**
   * Runs the migration tool on the referenced project.
   *
   * @param projectName the project to migrate
   * @return the path where the migrated project is located.
   */
  protected String migrate(String projectName, String... additionalParams) throws Exception {
    final String projectBasePath = new File(getResourceUri("e2e/" + projectName + "/input")).getAbsolutePath();

    final String outPutPath = migrationResult.getRoot().toPath().resolve(projectName).toAbsolutePath().toString();

    // Run migration tool
    final List<String> command = buildMigratorArgs(projectBasePath, outPutPath, projectName);
    Collections.addAll(command, additionalParams);
    int idx = command.indexOf("-parentDomainBasePath");
    if (idx != -1) {
      System.setProperty("parentDomainBasePath", command.get(idx + 1));
    }
    int run = MigrationRunner.run(command.toArray(new String[0]));
    assertEquals("Migration Failed", 0, run);
    return outPutPath;
  }

  private List<String> buildMigratorArgs(String projectBasePath, String outPutPath, String projectName) {
    final List<String> command = new ArrayList<>();
    command.add("-projectBasePath");
    command.add(projectBasePath);
    command.add("-destinationProjectBasePath");
    command.add(outPutPath);
    command.add("-muleVersion");
    command.add(RUNTIME_VERSION);
    command.add("-projectGAV");
    command.add(":" + projectName.replaceAll(".*/", "") + ":");
    command.add("-jsonReport");

    return command;
  }

  private void verifyOutput(String outputDir, String appName) throws URISyntaxException, IOException {
    Path expectedOutputBasePath = Paths.get(getResourceUri("e2e/" + appName + "/output"));
    Path outputBasePath = Paths.get(outputDir);

    Files.walk(expectedOutputBasePath).forEach(expectedPath -> {
      if (Files.isRegularFile(expectedPath)) {
        Path migratedPath = outputBasePath.resolve(expectedOutputBasePath.relativize(expectedPath));
        if (Files.exists(migratedPath)) {
          logger.info("Checking migrated file: {}", expectedOutputBasePath.getParent().relativize(expectedPath));
          if (migratedPath.toFile().getName().endsWith("pom.xml")) {
            comparePom(expectedPath, migratedPath);
          } else if (migratedPath.toFile().getName().endsWith(".xml")) {
            compareXml(expectedPath, migratedPath);
          } else if (migratedPath.toFile().getName().endsWith(".json")) {
            compareJson(expectedPath, migratedPath);
          } else if (migratedPath.toFile().getName().endsWith(".html")) {
            compareStringFiles(expectedPath, migratedPath);
          } else {
            compareChars(expectedPath, migratedPath);
          }
        } else {
          fail("Migrated file not found: " + migratedPath);
        }
      }
    });
  }

  private void comparePom(Path expectedPath, Path migratedPath) {
    try {
      MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
      StringWriter expectedOutput = new StringWriter();
      StringWriter migratedOutput = new StringWriter();
      mavenWriter.write(expectedOutput, new PomModelBuilder().withPom(expectedPath).build().getMavenModelCopy());
      mavenWriter.write(migratedOutput, new PomModelBuilder().withPom(migratedPath).build().getMavenModelCopy());
      Diff d = DiffBuilder.compare(Input.fromString(expectedOutput.toString()))
          .withTest(Input.fromString(migratedOutput.toString()))
          .build();
      assertFalse(d.toString(), d.hasDifferences());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void compareJson(Path expected, Path migratedPath) {
    try {
      JsonElement expectedJson = JsonParser.parseString(IOUtils.toString(expected.toUri(), UTF_8));
      JsonElement actualJson = JsonParser.parseString(IOUtils.toString(migratedPath.toUri(), UTF_8));
      if (migratedPath.getFileName().endsWith("report.json")) {
        normalizeFilePath(expectedJson);
        normalizeFilePath(actualJson);
      }
      assertEquals(expectedJson, actualJson);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void normalizeFilePath(JsonElement report) {
    String messagesKey = "detailedMessages";
    if (report.isJsonObject() && report.getAsJsonObject().has(messagesKey)) {
      report.getAsJsonObject().get(messagesKey).getAsJsonArray().forEach(e -> {
        String filePathKey = "filePath";
        if (e.isJsonObject() && e.getAsJsonObject().has(filePathKey)) {
          String filePath = e.getAsJsonObject().get(filePathKey).getAsString();
          if (filePath.contains("\\")) {
            e.getAsJsonObject().remove(filePathKey);
            e.getAsJsonObject().addProperty(filePathKey, filePath.replaceAll("\\\\", "/"));
          }
        }
      });
    }
  }

  protected URI getResourceUri(String path) throws URISyntaxException {
    URL resource = AbstractEndToEndTestCase.class.getClassLoader().getResource(path);
    assertNotNull("project not found at " + path, resource);
    return resource.toURI();
  }

  private void compareXml(Path output, Path expected) {
    Diff d = DiffBuilder.compare(Input.fromFile(expected.toFile()))
        .withTest(Input.fromFile(output.toFile()))
        .ignoreComments()
        .ignoreWhitespace()
        .ignoreElementContentWhitespace()
        .build();
    assertFalse(d.toString(), d.hasDifferences());
  }

  private void compareStringFiles(Path output, Path expected) {
    try {
      assertThat(String.format("Comparison mismatch at resource %s: ",
                               new File("test-classes").getAbsoluteFile().toPath().relativize(output)),
                 FileUtils.readFileToString(output.toFile(), "utf-8"),
                 Matchers.equalToIgnoringWhiteSpace(FileUtils.readFileToString(expected.toFile(), "utf-8")));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void compareChars(Path output, Path expected) {
    try {
      FileAssert
          .assertEquals(String.format("Comparison mismatch at resource %s: ",
                                      new File("test-classes").getAbsoluteFile().toPath().relativize(output)),
                        expected.toFile(), output.toFile());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void copyMigrationResult() {
    File migratedApps = new File("apps", getClass().getSimpleName());
    try {
      copyDirectory(migrationResult.getRoot(), migratedApps);
    } catch (IOException e) {
      logger.warn("Could not copy migration result.");
    }
  }

}
