/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.integration;

import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.infrastructure.server.ftp.EmbeddedFtpServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FtpMigrationTestCase extends EndToEndTestCase {

  private final String FTP_SERVER_BASE_DIR = Paths.get("target", "ftpserver").toString();
  private final String WORKING_DIR = "base";
  private final File serverBaseDir = new File(FTP_SERVER_BASE_DIR, WORKING_DIR);

  @Rule
  public final DynamicPort ftpSourcePort = new DynamicPort("ftpPort");

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
    createFtpServerBaseDir();

    ftpSourceServer = new EmbeddedFtpServer(ftpSourcePort.getNumber());
    ftpDestinationServer = new EmbeddedFtpServer(ftpDestinationPort.getNumber());

    ftpSourceServer.start();
    ftpDestinationServer.start();
  }

  private void createFtpServerBaseDir() throws IOException {
    this.deleteFtpServerBaseDir();
    this.serverBaseDir.mkdirs();
  }

  private void deleteFtpServerBaseDir() throws IOException {
    FileUtils.deleteDirectory(serverBaseDir);
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
