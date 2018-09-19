/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.infrastructure.server.ftp.EmbeddedFtpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FtpMigrationTestCase extends EndToEndTestCase {

  @Rule
  public TemporaryFolder sourceTemp = new TemporaryFolder();

  @Rule
  public final DynamicPort ftpSourcePort = new DynamicPort("ftpPort");

  @Rule
  public TemporaryFolder destinationTemp = new TemporaryFolder();

  @Rule
  public final DynamicPort ftpDestinationPort = new DynamicPort("ftpDestinationPort");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "ftp",
        "ftp-ee"
    };
  }

  private final String appToMigrate;

  public FtpMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  private EmbeddedFtpServer ftpSourceServer;
  private EmbeddedFtpServer ftpDestinationServer;

  @Before
  public void before() throws Exception {
    ftpSourceServer = new EmbeddedFtpServer(ftpSourcePort.getNumber());
    ftpDestinationServer = new EmbeddedFtpServer(ftpDestinationPort.getNumber());

    ftpSourceServer.start();
    ftpDestinationServer.start();
  }

  @After
  public void after() {
    ftpSourceServer.stop();
    ftpDestinationServer.stop();
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate, "-M-DftpSourcePort=" + ftpSourcePort.getNumber(),
               "-M-DftpDestinationPort=" + ftpDestinationPort.getNumber());
  }
}
