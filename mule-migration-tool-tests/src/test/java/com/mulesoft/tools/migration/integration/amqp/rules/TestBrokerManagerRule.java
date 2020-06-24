/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.integration.amqp.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.junit.rules.ExternalResource;

public class TestBrokerManagerRule extends ExternalResource {

  private static final String INITIAL_CONFIG_PATH =
      "../src/test/resources/e2e/amqp1Mvn/broker/broker_config.json";
  protected static final SystemLauncher broker = new SystemLauncher();

  public void startBroker() throws Exception {
    Map<String, Object> brokerOptions = new HashMap<>();
    brokerOptions.put("type", "Memory");
    brokerOptions.put("initialConfigurationLocation", INITIAL_CONFIG_PATH);
    brokerOptions.put("startupLoggedToSystemOut", true);
    broker.startup(brokerOptions);
  }

  @Override
  protected void before() throws Throwable {
    startBroker();
  }

  @Override
  protected void after() {
    stopBroker();
  }

  public void stopBroker() {
    broker.shutdown();
  }
}
