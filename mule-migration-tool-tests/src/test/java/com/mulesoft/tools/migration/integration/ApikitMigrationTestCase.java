/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mule.tck.junit4.rule.DynamicPort;

@RunWith(Parameterized.class)
public class ApikitMigrationTestCase extends EndToEndTestCase {

  @Rule
  public final DynamicPort httpPort = new DynamicPort("httpPort");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "apikit1",
        "apikit2"
    };
  }

  private final String appToMigrate;

  public ApikitMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate, "-M-DhttpPort=" + httpPort.getValue());
  }
}
