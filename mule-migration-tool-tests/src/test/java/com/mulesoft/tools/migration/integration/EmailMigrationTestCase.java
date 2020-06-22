/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.integration;

import org.mule.tck.junit4.rule.DynamicPort;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(Parameterized.class)
public class EmailMigrationTestCase extends EndToEndTestCase {

  private static final long SERVER_STARTUP_TIMEOUT = 5000;

  public static final String JUANI_EMAIL = "juan.desimoni@mulesoft.com";

  @Rule
  public DynamicPort imapPort = new DynamicPort("imapPort");
  @Rule
  public DynamicPort pop3Port = new DynamicPort("pop3Port");
  @Rule
  public DynamicPort smtpPort = new DynamicPort("smtpPort");

  protected GreenMail serverImap;
  protected GreenMailUser userImap;
  protected GreenMail serverPop3;
  protected GreenMailUser userPop3;
  protected GreenMail serverSmtp;
  protected GreenMailUser userSmtp;

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "email-1",
        "email-2"
    };
  }

  private final String appToMigrate;

  public EmailMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  @Before
  public void before() throws Exception {
    ServerSetup serverImapSetup = setUpServer(imapPort.getNumber(), "imap");
    serverImap = new GreenMail(serverImapSetup);
    serverImap.start();
    userImap = serverImap.setUser(JUANI_EMAIL, JUANI_EMAIL, "password");

    ServerSetup serverPop3Setup = setUpServer(pop3Port.getNumber(), "imap");
    serverPop3 = new GreenMail(serverPop3Setup);
    serverPop3.start();
    userImap = serverPop3.setUser(JUANI_EMAIL, JUANI_EMAIL, "password");

    ServerSetup serverSmtpSetup = setUpServer(smtpPort.getNumber(), "imap");
    serverSmtp = new GreenMail(serverSmtpSetup);
    serverSmtp.start();
    userSmtp = serverSmtp.setUser(JUANI_EMAIL, JUANI_EMAIL, "password");
  }

  public static ServerSetup setUpServer(int port, String protocol) {
    ServerSetup serverSetup = new ServerSetup(port, null, protocol);
    serverSetup.setServerStartupTimeout(SERVER_STARTUP_TIMEOUT);
    return serverSetup;
  }

  @After
  public void after() {
    serverImap.stop();
    serverPop3.stop();
    serverSmtp.stop();
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate, "-M-DimapPort=" + imapPort.getNumber(), "-M-Dpop3Port=" + pop3Port.getNumber(),
               "-M-DsmtpPort=" + smtpPort.getNumber());
  }
}
