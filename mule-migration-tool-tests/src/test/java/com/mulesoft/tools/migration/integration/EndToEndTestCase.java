/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.runtime.deployment.model.api.application.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;

import com.mulesoft.mule.distributions.server.AbstractEeAppControl;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Tests the whole migration process, starting with a Mule 3 source config, migrating it to Mule 4, packaging and deploying it to
 * a standalone runtime.
 */
public abstract class EndToEndTestCase extends AbstractEeAppControl {

  private static final String DELETE_ON_EXIT = getProperty("mule.test.deleteOnExit");

  @Rule
  public TemporaryFolder migrationResult = new TemporaryFolder();

  public void simpleCase(String appName, String... muleArgs) throws Exception {
    String projectBasePath =
        new File(EndToEndTestCase.class.getClassLoader().getResource("e2e/" + appName).toURI()).getAbsolutePath();

    File migrationResultFolder = migrationResult.newFolder(appName);

    // Run migration tool
    ProcessBuilder pb = new ProcessBuilder("java", "-jar", getProperty("migrator.runner"),
                                           "-projectBasePath", projectBasePath,
                                           "-destinationProjectBasePath", migrationResultFolder.getAbsolutePath(),
                                           "-migrationConfigurationPath", "");
    pb.redirectErrorStream(true);
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println("Migrator: " + line);
      }
    }

    BundleDescriptor migratedAppDescriptor = new BundleDescriptor.Builder().setGroupId("org.mule.migrated")
        .setArtifactId(appName).setVersion("1.0.0-SNAPSHOT").setClassifier(MULE_APPLICATION_CLASSIFIER).build();

    File migratedAppArtifact = installMavenArtifact(migrationResultFolder.getAbsolutePath(), migratedAppDescriptor);

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

  @Override
  public int getTestTimeoutSecs() {
    return super.getTestTimeoutSecs() * 2;
  }
}
