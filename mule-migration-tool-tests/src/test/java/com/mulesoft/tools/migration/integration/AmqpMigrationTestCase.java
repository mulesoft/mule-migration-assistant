/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
