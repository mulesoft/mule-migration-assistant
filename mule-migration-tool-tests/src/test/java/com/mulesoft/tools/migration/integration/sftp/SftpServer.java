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
package com.mulesoft.tools.migration.integration.sftp;

import static java.util.Arrays.asList;

import org.mule.runtime.api.exception.MuleRuntimeException;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Security;

public class SftpServer {

  public static final String USERNAME = "muletest1";
  public static final String PASSWORD = "muletest1";
  private SshServer sshdServer;
  private final Integer port;
  private final Path path;

  public SftpServer(int port, Path path) {
    this.port = port;
    this.path = path;
    configureSecurityProvider();
    SftpSubsystemFactory factory = createFtpSubsystemFactory();
    sshdServer = SshServer.setUpDefaultServer();
    configureSshdServer(factory);
  }

  private void configureSshdServer(SftpSubsystemFactory factory) {
    sshdServer.setPort(port);
    sshdServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("hostkey.ser")));
    sshdServer.setSubsystemFactories(asList(factory));
    sshdServer.setCommandFactory(new ScpCommandFactory());
    sshdServer.setFileSystemFactory(new VirtualFileSystemFactory(path));

    sshdServer.setPasswordAuthenticator((username, password, arg2) -> USERNAME.equals(username) && PASSWORD.equals(password));
  }

  private SftpSubsystemFactory createFtpSubsystemFactory() {
    return new SftpSubsystemFactory();
  }

  private void configureSecurityProvider() {
    Security.addProvider(new BouncyCastleProvider());
  }

  public void start() {
    try {
      if (sshdServer == null) {
        sshdServer = SshServer.setUpDefaultServer();
        configureSshdServer(createFtpSubsystemFactory());
      }
      sshdServer.start();
    } catch (IOException e) {
      throw new MuleRuntimeException(e);
    }
  }

  public void stop() {
    try {
      sshdServer.stop(false);
    } catch (IOException e) {
      throw new MuleRuntimeException(e);
    }
    sshdServer = null;
  }
}
