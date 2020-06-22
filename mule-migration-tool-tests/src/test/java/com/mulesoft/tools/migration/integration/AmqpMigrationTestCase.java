/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mulesoft.tools.migration.integration.amqp.rules.TestBrokerManagerRule;


@RunWith(Parameterized.class)
public class AmqpMigrationTestCase extends EndToEndTestCase {

  @ClassRule
  public static TestBrokerManagerRule testBrokerManagerRule = new TestBrokerManagerRule();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "amqp1Mvn"
    };
  }

  private final String appToMigrate;

  public AmqpMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate);
  }
}
