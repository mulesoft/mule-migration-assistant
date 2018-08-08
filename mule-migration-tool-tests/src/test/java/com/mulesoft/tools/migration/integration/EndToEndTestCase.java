/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import com.mulesoft.mule.distributions.server.AbstractEeAppControl;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.fail;
import static org.mule.runtime.deployment.model.api.application.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

/**
 * Tests the whole migration process, starting with a Mule 3 source config, migrating it to Mule 4, packaging and deploying it to
 * a standalone runtime.
 */
public abstract class EndToEndTestCase extends AbstractEeAppControl {

  private static final String DELETE_ON_EXIT = getProperty("mule.test.deleteOnExit");

  private static final String ONLY_MIGRATE = getProperty("mule.test.migratorOnly");

  private static final boolean DEBUG_RUNNER = Boolean.getBoolean("mule.test.debugRunner");

  @Rule
  public TemporaryFolder migrationResult = new TemporaryFolder();

  public void simpleCase(String appName, String... muleArgs) throws Exception {
    String projectBasePath =
        new File(EndToEndTestCase.class.getClassLoader().getResource("e2e/" + appName).toURI()).getAbsolutePath();

    String outPutPath = migrationResult.getRoot().toPath().resolve(appName).toAbsolutePath().toString();

    // Run migration tool
    final List<String> command = buildRunnerCommand(projectBasePath, outPutPath);
    ProcessBuilder pb = new ProcessBuilder(command);

    pb.redirectErrorStream(true);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println("Migrator: " + line);
      }
    }

    if (p.waitFor() != 0) {
      fail("Migration failed");
    }

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

  private List<String> buildRunnerCommand(String projectBasePath, String outPutPath) {
    final List<String> command = new ArrayList<>();
    command.add("java");

    if (DEBUG_RUNNER)
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
    return super.getTestTimeoutSecs() * 2;
  }
}
