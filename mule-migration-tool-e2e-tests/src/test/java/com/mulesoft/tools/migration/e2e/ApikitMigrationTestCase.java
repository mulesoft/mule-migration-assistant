/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.e2e;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ApikitMigrationTestCase extends AbstractEndToEndTestCase {

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "apikit/apikit1",
        "apikit/apikit2",
    };
  }

  private final String appToMigrate;

  public ApikitMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate);
  }
}
