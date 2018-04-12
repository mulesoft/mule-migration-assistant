/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import static org.mule.runtime.deployment.model.api.application.ApplicationDescriptor.MULE_APPLICATION_CLASSIFIER;
import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;

import com.mulesoft.mule.distributions.server.AbstractEeAppControl;
import com.mulesoft.tools.migration.MigrationRunner;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Tests the whole migration process, starting with a Mule 3 source config, migrating it to Mule 4, packaging and deploying it to
 * a standalone runtime.
 */
public abstract class EndToEndTestCase extends AbstractEeAppControl {

  @Rule
  public TemporaryFolder migrationResult = new TemporaryFolder();

  public void simpleCase(String appName) throws Exception {
    String projectBasePath =
        new File(EndToEndTestCase.class.getClassLoader().getResource("e2e/" + appName).toURI()).getAbsolutePath();

    File migrationResultFolder = migrationResult.newFolder(appName);

    // Run migration tool
    // TODO Use the actual command line instead of running through java
    MigrationRunner.main(new String[] {
        "-projectBasePath", projectBasePath,
        "-destinationProjectBasePath", migrationResultFolder.getAbsolutePath(),
        "-migrationConfigurationPath", ""
    });

    // TODO generated GAV should be consistent with source app
    BundleDescriptor migratedAppDescriptor = new BundleDescriptor.Builder().setGroupId("org.mule.migrated")
        .setArtifactId("migrated-project").setVersion("1.0.0").setClassifier(MULE_APPLICATION_CLASSIFIER).build();

    File migratedAppArtifact = installMavenArtifact(migrationResultFolder.getAbsolutePath(), migratedAppDescriptor);

    getMule().start();
    getMule().deploy(migratedAppArtifact.getAbsolutePath());
    assertAppIsDeployed(migratedAppDescriptor.getArtifactFileName());
  }
}
