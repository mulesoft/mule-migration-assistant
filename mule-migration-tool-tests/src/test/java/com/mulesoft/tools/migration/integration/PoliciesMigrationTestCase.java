/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
