/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static com.mulesoft.tools.migration.MigrationRunner.NO_COMPATIBILITY;


@RunWith(Parameterized.class)
public class IntegrationAppsTestCase extends AbstractEndToEndTestCase {

  @Parameters(name = "{0}-nc-{1}")
  public static Object[][] params() {
    return new Object[][] {
        {"integration/demo_connect", false},
        {"integration/demo_connect", true}
    };
  }

  private final String appToMigrate;
  private final boolean noCompatibility;

  public IntegrationAppsTestCase(String appToMigrate, boolean noCompatibility) {
    this.appToMigrate = appToMigrate;
    this.noCompatibility = noCompatibility;
  }

  @Test
  public void test() throws Exception {
    if (noCompatibility) {
      simpleCase(appToMigrate, "-" + NO_COMPATIBILITY);
    } else {
      simpleCase(appToMigrate);
    }
  }
}
