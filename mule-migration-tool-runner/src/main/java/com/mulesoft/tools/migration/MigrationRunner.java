/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration;

import static com.mulesoft.tools.migration.printer.ConsolePrinter.log;
import static com.mulesoft.tools.migration.printer.ConsolePrinter.printMigrationError;
import static com.mulesoft.tools.migration.printer.ConsolePrinter.printMigrationSummary;
import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.net.URLEncoder.encode;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.http.HttpVersion.HTTP_1_1;
import static org.apache.http.client.fluent.Executor.newInstance;

import com.mulesoft.tools.migration.engine.MigrationJob;
import com.mulesoft.tools.migration.engine.MigrationJob.MigrationJobBuilder;
import com.mulesoft.tools.migration.exception.ConsoleOptionsException;
import com.mulesoft.tools.migration.report.DefaultMigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;

import java.io.InputStream;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Base entry point to run {@link AbstractMigrationTask}s
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationRunner {

  private final static String HELP = "help";

  private final static String PROJECT_BASE_PATH = "projectBasePath";
  private final static String PARENT_DOMAIN_BASE_PATH = "parentDomainBasePath";
  private final static String DESTINATION_PROJECT_BASE_PATH = "destinationProjectBasePath";
  private final static String MULE_VERSION = "muleVersion";
  private final static String REPORT_HOME = "summary.html";
  public static final String MULE_3_VERSION = "3.*.*";

  private String projectBasePath;
  private String parentDomainProjectBasePath;
  private String destinationProjectBasePath;
  private String muleVersion;

  public static void main(String args[]) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();

    Gson gson = new Gson();

    //    SSLContext sslContext = SSLContexts.custom()
    //        .loadKeyMaterial(readStore(), null) // use null as second param if you don't have a separate key
    //        // password
    //        .build();

    // SSLContextBuilder builder = new SSLContextBuilder();
    // builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
    // SSLContext lalala = builder.build();

    Executor httpExecutor =
        newInstance(HttpClients.custom()/* .setSSLSocketFactory(new SSLConnectionSocketFactory(lalala)) */.build());
    //    Executor httpExecutor = newInstance(HttpClients.custom()/* .setSSLContext(sslContext) */.build());

    Request statisticsPost = Request.Post("https://localhost:8082/api/v1/migrated" +
        format("?%s&status=%d&userId=%s&mmtVersion=%s&osName=%s&osVersion=%s",
               getBoolean("mmt.uploadReport") ? "_" : "dryRun", 0, randomUUID().toString(), "0.3.0-SNAPSHOT",
               encode(getProperty("os.name"), "UTF-8"), encode(getProperty("os.version"), "UTF-8")))
        .version(HTTP_1_1);

    try {
      MigrationRunner migrationRunner = new MigrationRunner();
      migrationRunner.initializeOptions(args);

      MigrationJob job = migrationRunner.buildMigrationJob();
      DefaultMigrationReport report = new DefaultMigrationReport();
      log("Executing migrator " + job.getRunnerVersion() + "...");
      job.execute(report);

      httpExecutor
          .execute(statisticsPost.bodyString(gson.toJson(report), ContentType.APPLICATION_JSON))
          .discardContent();

      printMigrationSummary(job.getReportPath().resolve(REPORT_HOME).toAbsolutePath().toString(),
                            stopwatch.stop().elapsed(MILLISECONDS), report);
      exit(0);
    } catch (Exception ex) {
      httpExecutor
          .execute(statisticsPost.bodyString(gson.toJson(ex), ContentType.APPLICATION_JSON))
          .discardContent();

      printMigrationError(ex, stopwatch.stop().elapsed(MILLISECONDS));
      exit(-1);
    }
  }

  private static KeyStore readStore() throws Exception {
    try (InputStream keyStoreStream = MigrationRunner.class.getResourceAsStream("keystore.jks")) {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(keyStoreStream, "mulemanishere".toCharArray());
      return keyStore;
    }
  }

  private MigrationJob buildMigrationJob() throws Exception {
    MigrationJobBuilder builder = new MigrationJobBuilder()
        .withProject(Paths.get(projectBasePath))
        .withParentDomainProject(parentDomainProjectBasePath != null ? Paths.get(parentDomainProjectBasePath) : null)
        .withOutputProject(Paths.get(destinationProjectBasePath))
        .withInputVersion(MULE_3_VERSION)
        .withOuputVersion(muleVersion);
    return builder.build();
  }

  /**
   * Initialises the console options with Apache Command Line
   *
   * @param args
   */
  private void initializeOptions(String[] args) {

    Options options = new Options();

    options.addOption(HELP, false, "Shows the help");
    options.addOption(PROJECT_BASE_PATH, true, "Base directory of the project to be migrated");
    options.addOption(PARENT_DOMAIN_BASE_PATH, true, "Base directory of the parent domain of the project to be migrated, if any");
    options.addOption(DESTINATION_PROJECT_BASE_PATH, true, "Base directory of the migrated project");
    options.addOption(MULE_VERSION, true, "Mule version where to migrate project");

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine line = parser.parse(options, args);

      if (line.hasOption(PROJECT_BASE_PATH)) {
        this.projectBasePath = line.getOptionValue(PROJECT_BASE_PATH);
      } else {
        throw new ConsoleOptionsException("You must specify a project base path of the files to be migrated");
      }

      if (line.hasOption(PARENT_DOMAIN_BASE_PATH)) {
        this.parentDomainProjectBasePath = line.getOptionValue(PARENT_DOMAIN_BASE_PATH);
      }

      if (line.hasOption(DESTINATION_PROJECT_BASE_PATH)) {
        this.destinationProjectBasePath = line.getOptionValue(DESTINATION_PROJECT_BASE_PATH);
      } else {
        throw new ConsoleOptionsException("You must specify a destination project base path");
      }

      if (line.hasOption(MULE_VERSION)) {
        this.muleVersion = line.getOptionValue(MULE_VERSION);
      } else {
        throw new ConsoleOptionsException("You must specify a destination project base path");
      }

      if (line.hasOption(HELP)) {
        printHelp(options);
      }
    } catch (ParseException e) {
      e.printStackTrace();
      System.exit(-1);
    } catch (ConsoleOptionsException e) {
      printHelp(options);
      System.exit(-1);
    }
  }

  private void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("migration-tool - Help", options);
  }

}
