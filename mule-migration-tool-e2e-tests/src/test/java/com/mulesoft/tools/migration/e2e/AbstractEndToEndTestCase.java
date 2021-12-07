/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import static java.lang.System.getProperty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.mulesoft.tools.migration.MigrationRunner;
import com.mulesoft.tools.migration.engine.project.structure.ApplicationPersister;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel.PomModelBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junitx.framework.FileAssert;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
    System.setProperty(MigrationRunner.JSON_REPORT_PROP_NAME, "true");

    // Run migration tool
    final List<String> command = buildMigratorArgs(projectBasePath, outPutPath, projectName);
    Collections.addAll(command, additionalParams);
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
    command.add(":" + projectName + ":");

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
      JsonElement expectedJson = JsonParser.parseString(IOUtils.toString(expected.toUri(), StandardCharsets.UTF_8));
      JsonElement actualJson = JsonParser.parseString(IOUtils.toString(migratedPath.toUri(), StandardCharsets.UTF_8));
      assertEquals(expectedJson, actualJson);
    } catch (IOException e) {
      throw new RuntimeException(e);
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
