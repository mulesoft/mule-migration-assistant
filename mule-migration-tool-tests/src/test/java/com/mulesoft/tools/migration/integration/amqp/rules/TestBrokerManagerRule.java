/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration.amqp.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.junit.rules.ExternalResource;

public class TestBrokerManagerRule extends ExternalResource {

  private static final String INITIAL_CONFIG_PATH =
      "/Users/fabian.gonzalez/projects/mule-migration-tool/mule-migration-tool-tests/src/test/resources/e2e/amqp1Mvn/broker/broker_config.json";
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
