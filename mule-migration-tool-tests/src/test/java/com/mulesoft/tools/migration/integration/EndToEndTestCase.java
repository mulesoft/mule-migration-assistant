/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.fail;
import static org.mule.runtime.deployment.model.api.application.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;

import com.mulesoft.mule.distributions.server.AbstractEeAppControl;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the whole migration process, starting with a Mule 3 source config, migrating it to Mule 4, packaging and deploying it to
 * a standalone runtime.
 */
public abstract class EndToEndTestCase extends AbstractEeAppControl {

  private static final String DELETE_ON_EXIT = getProperty("mule.test.deleteOnExit");

  protected static final String ONLY_MIGRATE = getProperty("mule.test.migratorOnly");

  private static final String DEBUG_RUNNER = getProperty("mule.test.debugRunner");

  @Rule
  public TemporaryFolder migrationResult = new TemporaryFolder();

  public void simpleCase(String appName, String... muleArgs) throws Exception {
    String outPutPath = migrate(appName);

    if (ONLY_MIGRATE != null) {
      return;
    }

    BundleDescriptor migratedAppDescriptor = new BundleDescriptor.Builder().setGroupId("org.mule.migrated")
        .setArtifactId(appName).setVersion("1.0.0-M4-SNAPSHOT").setClassifier(MULE_APPLICATION_CLASSIFIER).build();

    File migratedAppArtifact = installMavenArtifact(outPutPath, migratedAppDescriptor);

    try {
      getMule().start(muleArgs);
      assertAppNotDeployed(migratedAppDescriptor.getArtifactFileName());
      getMule().deploy(migratedAppArtifact.getAbsolutePath());
      assertAppIsDeployed(migratedAppDescriptor.getArtifactFileName());
    } finally {
      getMule().stop();
      if (isEmpty(DELETE_ON_EXIT) || parseBoolean(DELETE_ON_EXIT)) {
        getMule().undeployAll();
      }
    }
  }

  /**
   * Runs the migration tool on the referenced project.
   *
   * @param projectName
   * @return the path where the migrated project is located.
   * @throws URISyntaxException
   * @throws IOException
   * @throws InterruptedException
   */
  protected String migrate(String projectName) throws URISyntaxException, IOException, InterruptedException {
    String projectBasePath =
        new File(EndToEndTestCase.class.getClassLoader().getResource("e2e/" + projectName).toURI()).getAbsolutePath();

    String outPutPath = migrationResult.getRoot().toPath().resolve(projectName).toAbsolutePath().toString();

    // Run migration tool
    final List<String> command = buildRunnerCommand(projectBasePath, outPutPath);
    ProcessBuilder pb = new ProcessBuilder(command);

    pb.redirectErrorStream(true);
    Process p = pb.start();

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        p.destroy();
      }
    });

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

  private List<String> buildRunnerCommand(String projectBasePath, String outPutPath) {
    final List<String> command = new ArrayList<>();
    command.add("java");

    if (DEBUG_RUNNER != null)
      command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000");

    command.add("-jar");
    command.add(getProperty("migrator.runner"));
    command.add("-projectBasePath");
    command.add(projectBasePath);
    command.add("-destinationProjectBasePath");
    command.add(outPutPath);
    command.add("-muleVersion");
    command.add(getProperty("mule.version"));

    return command;
  }

  @Override
  public int getTestTimeoutSecs() {
    if (DEBUG_RUNNER == null) {
      return super.getTestTimeoutSecs() * 2;
    } else {
      return MAX_VALUE / 1000;
    }
  }
}
