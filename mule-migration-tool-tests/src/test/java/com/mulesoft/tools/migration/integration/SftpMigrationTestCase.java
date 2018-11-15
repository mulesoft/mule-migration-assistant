/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import org.mule.tck.junit4.rule.DynamicPort;

import com.mulesoft.tools.migration.integration.sftp.SftpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SftpMigrationTestCase extends EndToEndTestCase {

  @Rule
  public TemporaryFolder sourceTemp = new TemporaryFolder();

  @Rule
  public final DynamicPort sftpSourcePort = new DynamicPort("sftpPort");

  @Rule
  public TemporaryFolder destinationTemp = new TemporaryFolder();

  @Rule
  public final DynamicPort sftpDestinationPort = new DynamicPort("sftpDestinationPort");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "sftp"
    };
  }

  private final String appToMigrate;

  public SftpMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  private SftpServer sftpSourceServer;
  private SftpServer sftpDestinationServer;

  @Before
  public void before() throws Exception {
    sftpSourceServer = new SftpServer(sftpSourcePort.getNumber(), sourceTemp.newFolder().getAbsoluteFile().toPath());
    sftpDestinationServer =
        new SftpServer(sftpDestinationPort.getNumber(), destinationTemp.newFolder().getAbsoluteFile().toPath());

    sftpSourceServer.start();
    sftpDestinationServer.start();
  }

  @After
  public void after() {
    sftpSourceServer.stop();
    sftpDestinationServer.stop();
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate, "-M-DsftpSourcePort=" + sftpSourcePort.getNumber(),
               "-M-DsftpDestinationPort=" + sftpDestinationPort.getNumber());
  }
}
