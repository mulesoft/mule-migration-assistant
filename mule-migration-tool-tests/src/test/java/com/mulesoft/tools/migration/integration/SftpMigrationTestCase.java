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

import static org.apache.commons.io.FileUtils.forceMkdir;

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

import java.io.File;

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
    File sourceRoot = sourceTemp.newFolder();
    forceMkdir(new File(sourceRoot, "source"));
    sftpSourceServer = new SftpServer(sftpSourcePort.getNumber(), sourceRoot.toPath());

    File targetRoot = destinationTemp.newFolder();
    forceMkdir(new File(targetRoot, "target"));
    sftpDestinationServer = new SftpServer(sftpDestinationPort.getNumber(), targetRoot.toPath());

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
