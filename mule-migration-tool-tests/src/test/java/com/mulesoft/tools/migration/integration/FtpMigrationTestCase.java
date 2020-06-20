/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
