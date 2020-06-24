/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.integration;

import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;

@RunWith(Parameterized.class)
public class DomainsMigrationTestCase extends EndToEndTestCase {

  @Rule
  public final DynamicPort httpPort = new DynamicPort("httpPort");

  @Parameters(name = "{0}, {1}")
  public static Object[][] params() {
    return new Object[][] {
        new Object[] {"domain1", "domain1app1"}
    };
  }

  private final String domainToMigrate;
  private final String appToMigrate;

  public DomainsMigrationTestCase(String domainToMigrate, String appToMigrate) {
    this.domainToMigrate = domainToMigrate;
    this.appToMigrate = appToMigrate;
  }

  private File migratedDomainArtifact;
  private File migratedAppArtifact;

  @Test
  public void test() throws Exception {
    String outPutDomainPath = migrate(domainToMigrate);
    String outPutAppPath = migrate(appToMigrate, "-parentDomainBasePath",
                                   new File(EndToEndTestCase.class.getClassLoader().getResource("e2e/" + domainToMigrate).toURI())
                                       .getAbsolutePath());

    if (ONLY_MIGRATE != null) {
      return;
    }

    BundleDescriptor migratedDomainDescriptor = new BundleDescriptor.Builder().setGroupId("org.mule.migrated")
        .setArtifactId(domainToMigrate).setVersion("1.0.0-M4-SNAPSHOT").setClassifier("mule-domain").build();

    migratedDomainArtifact = installMavenArtifact(outPutDomainPath, migratedDomainDescriptor);

    BundleDescriptor migratedAppDescriptor = new BundleDescriptor.Builder().setGroupId("org.mule.migrated")
        .setArtifactId(appToMigrate).setVersion("1.0.0-M4-SNAPSHOT").setClassifier("mule-application").build();

    migratedAppArtifact = installMavenArtifact(outPutAppPath, migratedAppDescriptor);

    startStopMule(migratedAppDescriptor, migratedAppArtifact, "-M-DhttpPort=" + httpPort.getValue());
  }

  @Override
  protected void deployArtifactsToMule(File m) {
    getMule().deployDomain(migratedDomainArtifact.getAbsolutePath());
    getMule().deploy(migratedAppArtifact.getAbsolutePath());
  }

}
