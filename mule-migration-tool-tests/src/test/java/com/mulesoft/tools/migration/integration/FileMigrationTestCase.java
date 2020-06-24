/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.integration;

import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;

@RunWith(Parameterized.class)
public class FileMigrationTestCase extends EndToEndTestCase {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public final DynamicPort httpPort = new DynamicPort("httpPort");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "file1",
        "file2"
    };
  }

  private final String appToMigrate;

  public FileMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  public void test() throws Exception {
    File workingDir = temp.newFolder();
    new File(workingDir, "input").mkdir();
    simpleCase(appToMigrate, "-M-DworkingDirectory=" + workingDir.getAbsolutePath());
  }
}
