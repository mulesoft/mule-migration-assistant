/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MUnitMigrationTestCase extends EndToEndTestCase {

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "munitSimple",
        "munitSimpleMvn",
        "munitMockMvn"
    };
  }

  private final String appToMigrate;

  public MUnitMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate);
  }
}
