/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class TransformTestCase extends EndToEndTestCase {

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "withsampledatafiles"
    };
  }

  private final String appToMigrate;

  public TransformTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate);

    final Path baseMigratedPath = migrationResult.getRoot().toPath().resolve(appToMigrate);

    assertThat(baseMigratedPath.resolve("randomFileInRoot.txt").toFile().exists(), is(true));

    assertThat(baseMigratedPath
        .resolve(Paths.get("src", "main", "resources", "catalog", "types", "custom", "myType__md__custom_type__.xml")).toFile()
        .exists(), is(true));
    assertThat(baseMigratedPath
        .resolve(Paths.get("src", "main", "resources", "catalog", "types", "custom", "myOtherType__md__custom_type__.xml"))
        .toFile().exists(), is(true));
  }
}
