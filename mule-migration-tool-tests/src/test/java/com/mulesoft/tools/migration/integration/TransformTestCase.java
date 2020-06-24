/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
