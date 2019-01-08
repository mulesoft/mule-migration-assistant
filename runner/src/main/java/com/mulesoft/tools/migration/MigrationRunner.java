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
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.net.URLEncoder.encode;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.http.HttpVersion.HTTP_1_1;
import static org.apache.http.client.fluent.Executor.newInstance;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

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
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClients;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;

import java.nio.file.Paths;

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

  private String userId;
  private String sessionId;
  private String proxyHost;
  private Integer proxyPort;
  private String proxyUser;
  private String proxyPass;

  public static void main(String args[]) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();

    MigrationRunner migrationRunner = buildRunner(args);
    MigrationJob job = migrationRunner.buildMigrationJob();

    try {
      DefaultMigrationReport report = new DefaultMigrationReport();
      log("Executing migrator " + job.getRunnerVersion() + "...");
      job.execute(report);

      migrationRunner.sendUsageStatistics(job, report);

      printMigrationSummary(job.getReportPath().resolve(REPORT_HOME).toAbsolutePath().toString(),
                            stopwatch.stop().elapsed(MILLISECONDS), report);
      exit(0);
    } catch (Exception ex) {
      migrationRunner.sendUsageStatistics(job, ex);

      printMigrationError(ex, stopwatch.stop().elapsed(MILLISECONDS));
      exit(-1);
    }
  }

  protected static MigrationRunner buildRunner(String[] args) throws Exception {
    MigrationRunner migrationRunner = new MigrationRunner();
    migrationRunner.initializeOptions(args);

    return migrationRunner;
  }

  private MigrationJob buildMigrationJob() throws Exception {
    return new MigrationJobBuilder()
        .withProject(Paths.get(projectBasePath))
        .withParentDomainProject(parentDomainProjectBasePath != null ? Paths.get(parentDomainProjectBasePath) : null)
        .withOutputProject(Paths.get(destinationProjectBasePath))
        .withInputVersion(MULE_3_VERSION)
        .withOuputVersion(muleVersion)
        .build();
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

    options.addOption("userId", true, "The userId to send for the usage statistics");
    options.addOption("sessionId", true, "The sessionId to send for the usage statistics");
    options.addOption("proxyHost", true, "The host of the proxy to use when sending usage statistics");
    options.addOption("proxyPort", true, "The port of the proxy to use when sending usage statistics");
    options.addOption("proxyUser", true, "The username of the proxy to use when sending usage statistics");
    options.addOption("proxyPass", true, "The password of the proxy to use when sending usage statistics");

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

      if (line.hasOption("userId")) {
        this.userId = line.getOptionValue("userId");
      } else {
        this.userId = randomUUID().toString();
      }
      if (line.hasOption("sessionId")) {
        this.sessionId = line.getOptionValue("sessionId");
      } else {
        this.sessionId = "111111111";
      }
      if (line.hasOption("proxyHost")) {
        this.proxyHost = line.getOptionValue("proxyHost");
      }
      if (line.hasOption("proxyPort")) {
        this.proxyPort = parseInt(line.getOptionValue("proxyPort"));
      }
      if (line.hasOption("proxyUser")) {
        this.proxyUser = line.getOptionValue("proxyUser");
      }
      if (line.hasOption("proxyPass")) {
        this.proxyPass = line.getOptionValue("proxyPass");
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
    formatter.printHelp("migration-assistant - Help", options);
  }

  protected void sendUsageStatistics(MigrationJob job, Object body) {
    if (System.getProperty("mmt.disableUsageStatistics") != null) {
      return;
    }

    try {
      Gson gson = new Gson();

      Executor httpExecutor = newInstance(HttpClients.custom().build());

      if (proxyUser != null && proxyPass != null) {
        httpExecutor = httpExecutor.auth(new HttpHost(proxyHost, proxyPort), proxyUser, proxyPass);
      }

      Request request = Request.Post("https://mmt-stats-gatherer.us-e1.cloudhub.io/api/v1/migrated" +
          format("?status=%d&userId=%s&sessionId=%s&mmtVersion=%s&osName=%s&osVersion=%s",
                 0, userId, sessionId, job.getRunnerVersion(),
                 encode(getProperty("os.name"), "UTF-8"), encode(getProperty("os.version"), "UTF-8")))
          .version(HTTP_1_1)
          .bodyString(gson.toJson(body), APPLICATION_JSON);

      if (proxyHost != null) {
        request = request.viaProxy(new HttpHost(proxyHost, proxyPort));
      }

      httpExecutor.execute(request).handleResponse(response -> {
        return response;
      });
    } catch (Exception e) {
      // Nothing to do, do not fail the migration just for being unable to send the statistics.
    }
  }

}
