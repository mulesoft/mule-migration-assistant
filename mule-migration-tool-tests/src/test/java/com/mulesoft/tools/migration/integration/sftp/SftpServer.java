/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
