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
