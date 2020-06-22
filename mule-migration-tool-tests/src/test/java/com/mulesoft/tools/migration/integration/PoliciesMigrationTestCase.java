/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import static org.mule.test.infrastructure.maven.MavenTestUtils.installMavenArtifact;

import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;

@Ignore("AGW-2744 - Uncomment when policies are properly migrated.")
@RunWith(Parameterized.class)
public class PoliciesMigrationTestCase extends EndToEndTestCase {

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "policySimple"
    };
  }

  private final String policyToMigrate;

  public PoliciesMigrationTestCase(String policyToMigrate) {
    this.policyToMigrate = policyToMigrate;
  }

  @Test
  public void test() throws Exception {
    String outPutPath = migrate(policyToMigrate);

    if (ONLY_MIGRATE != null) {
      return;
    }

    BundleDescriptor migratedPolicyDescriptor = new BundleDescriptor.Builder().setGroupId("org.mule.migrated")
        .setArtifactId(policyToMigrate).setVersion("1.0.0-M4-SNAPSHOT").setClassifier("mule-policy").build();

    File migratedAppArtifact = installMavenArtifact(outPutPath, migratedPolicyDescriptor);
  }

}
