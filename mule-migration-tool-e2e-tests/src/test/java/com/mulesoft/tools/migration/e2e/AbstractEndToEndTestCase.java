/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import static java.io.File.separator;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.BeforeClass;
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

  private static final String RUNTIME_VERSION = getProperty("mule.version");

  private static final String DEBUG_RUNNER = getProperty("mule.test.debugRunner");

  @ClassRule
  public static TemporaryFolder mmaBinary = new TemporaryFolder();

  private static File mmaBinaryFolder;

  private static final Logger logger = LoggerFactory.getLogger(AbstractEndToEndTestCase.class);

  @Rule
  public TemporaryFolder migrationResult = new TemporaryFolder();

  @BeforeClass
  public static void prepareMma() throws IOException {
    mmaBinaryFolder = mmaBinary.newFolder();

    try (ZipFile zip = new ZipFile(new File(getProperty("migrator.runner")))) {
      Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
      ZipEntry root = zipFileEntries.nextElement();
      File mmaRootFile = new File(mmaBinaryFolder, root.getName());
      mmaRootFile.mkdirs();
      while (zipFileEntries.hasMoreElements()) {
        ZipEntry entry = zipFileEntries.nextElement();
        File destFile = new File(mmaBinaryFolder, entry.getName());
        if (entry.isDirectory()) {
          destFile.mkdir();
        } else {
          copyInputStreamToFile(zip.getInputStream(entry), destFile);
        }
      }
    }

  }

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
    String projectBasePath = new File(getResourceUri("e2e/" + projectName + "/input")).getAbsolutePath();

    String outPutPath = migrationResult.getRoot().toPath().resolve(projectName).toAbsolutePath().toString();

    // Run migration tool
    final List<String> command = buildRunnerCommand(projectBasePath, outPutPath, projectName);
    Collections.addAll(command, additionalParams);
    ProcessBuilder pb = new ProcessBuilder(command);

    pb.redirectErrorStream(true);
    Process p = pb.start();

    Runtime.getRuntime().addShutdownHook(new Thread(p::destroy));

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println("Migrator: " + line);
      }
    }

    if (p.waitFor() != 0) {
      fail("Migration failed");
    }
    return outPutPath;
  }

  private List<String> buildRunnerCommand(String projectBasePath, String outPutPath, String projectName) {
    final List<String> command = new ArrayList<>();
    command.add("java");

    if (DEBUG_RUNNER != null) {
      command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000");
    }

    command.add("-jar");
    command.add(mmaBinaryFolder.getAbsolutePath() + separator
        + "mule-migration-assistant-runner-" + getProperty("mma.version") + ".jar");
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
          if (migratedPath.toFile().getName().endsWith(".xml")) {
            compareXml(expectedPath, migratedPath);
          } else {
            compareChars(expectedPath, migratedPath);
          }
        } else {
          fail("Migrated file not found: " + migratedPath);
        }
      }
    });
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
      assertTrue(IOUtils.contentEquals(new FileReader(output.toFile()), new FileReader(expected.toFile())));
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
