/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration;

import static com.mulesoft.tools.migration.printer.ConsolePrinter.log;
import static com.mulesoft.tools.migration.printer.ConsolePrinter.printMigrationError;
import static com.mulesoft.tools.migration.printer.ConsolePrinter.printMigrationSummary;
import static java.lang.Integer.parseInt;
import static java.lang.System.exit;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.mulesoft.tools.migration.engine.MigrationJob;
import com.mulesoft.tools.migration.engine.MigrationJob.MigrationJobBuilder;
import com.mulesoft.tools.migration.exception.ConsoleOptionsException;
import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.project.model.pom.Parent.ParentBuilder;
import com.mulesoft.tools.migration.report.DefaultMigrationReport;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import com.google.common.base.Stopwatch;

import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

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
  private final static String CANCEL_ON_ERROR = "cancelOnError";
  private final static String PROJECT_PARENT_GAV = "projectParentGAV";
  private final static String PROJECT_GAV = "projectGAV";
  public static final String JSON_REPORT = "jsonReport";
  public static final String NO_COMPATIBILITY = "noCompatibility";

  private String projectBasePath;
  private String parentDomainProjectBasePath;
  private String destinationProjectBasePath;
  private String muleVersion;
  private boolean cancelOnError = false;
  private Parent projectParentGAV;
  private String projectGAV;
  private boolean jsonReport;
  private boolean noCompatibility;

  private String userId;
  private String sessionId;
  private String proxyHost;
  private Integer proxyPort;
  private String proxyUser;
  private String proxyPass;

  public static void main(String args[]) throws Exception {
    exit(run(args));
  }

  public static int run(String[] args) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();

    Optional<MigrationRunner> migrationRunner = buildRunner(args);
    if (!migrationRunner.isPresent()) {
      return -1;
    }
    MigrationJob job = migrationRunner.get().buildMigrationJob();

    try {
      DefaultMigrationReport report = new DefaultMigrationReport();
      log("Executing migrator " + job.getRunnerVersion() + "...");
      job.execute(report);

      printMigrationSummary(job.getReportPath().resolve(REPORT_HOME).toAbsolutePath().toString(),
                            stopwatch.stop().elapsed(MILLISECONDS), report);
      return 0;
    } catch (Exception ex) {
      printMigrationError(ex, stopwatch.stop().elapsed(MILLISECONDS));
      return -1;
    }
  }

  protected static Optional<MigrationRunner> buildRunner(String[] args) throws Exception {
    MigrationRunner migrationRunner = new MigrationRunner();
    if (!migrationRunner.initializeOptions(args)) {
      return Optional.empty();
    } else {
      return Optional.of(migrationRunner);
    }
  }

  private MigrationJob buildMigrationJob() throws Exception {
    return new MigrationJobBuilder()
        .withProject(Paths.get(projectBasePath))
        .withParentDomainProject(parentDomainProjectBasePath != null ? Paths.get(parentDomainProjectBasePath) : null)
        .withOutputProject(Paths.get(destinationProjectBasePath))
        .withInputVersion(MULE_3_VERSION)
        .withOuputVersion(muleVersion)
        .withCancelOnError(cancelOnError)
        .withProjectParentGAV(projectParentGAV)
        .withProjectGAV(projectGAV)
        .withJsonReport(jsonReport)
        .withNoCompatibility(noCompatibility)
        .build();
  }

  /**
   * Initialises the console options with Apache Command Line
   *
   * @param args
   * @return
   */
  private boolean initializeOptions(String[] args) {

    Options options = new Options();

    options.addOption(HELP, false, "Shows the help");
    options.addOption(PROJECT_BASE_PATH, true, "Base directory of the project to be migrated");
    options.addOption(PARENT_DOMAIN_BASE_PATH, true, "Base directory of the parent domain of the project to be migrated, if any");
    options.addOption(DESTINATION_PROJECT_BASE_PATH, true, "Base directory of the migrated project");
    options.addOption(MULE_VERSION, true, "Mule version where to migrate project");
    options.addOption(CANCEL_ON_ERROR, true, "Use cancelOnError to stop the migration. Default is false");
    options.addOption(PROJECT_PARENT_GAV, true, "Use projectParentGAV to migration parent in your pom.xml");
    options.addOption(PROJECT_GAV, true, "Use projectGAV to override default GAV coordinates when a pom.xml is not provided");
    options.addOption(JSON_REPORT, false, "Generate migration report in JSON format");
    options.addOption(NO_COMPATIBILITY, false, "Do not use compatibility module");

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
        throw new ConsoleOptionsException("You must specify a target mule version");
      }

      if (line.hasOption(CANCEL_ON_ERROR)) {
        final String value = line.getOptionValue(CANCEL_ON_ERROR);
        if ("true".equals(value) || "false".equals(value)) {
          this.cancelOnError = Boolean.getBoolean(value);
        } else {
          throw new ConsoleOptionsException("You must specify a boolean value (true or false) for the 'cancelOnError' option");
        }

      }

      if (line.hasOption(PROJECT_PARENT_GAV)) {
        final String value = line.getOptionValue(PROJECT_PARENT_GAV);
        if (StringUtils.isNotEmpty(value) && value.split(":").length == 3) {
          String[] gav = value.split(":");
          this.projectParentGAV = new ParentBuilder()
              .withGroupId(gav[0])
              .withArtifactId(gav[1])
              .withVersion(gav[2])
              .build();
        } else {
          throw new ConsoleOptionsException("You must specify the GAV (groupId, artifactId and version) for the 'projectParentGAV' option");
        }
      }

      if (line.hasOption(PROJECT_GAV)) {
        final String value = line.getOptionValue(PROJECT_GAV);
        if (StringUtils.isNotEmpty(value) && value.matches(".*:.*:.*")) {
          this.projectGAV = value;
        } else {
          throw new ConsoleOptionsException("You must specify the GAV (groupId:artifactId:version) for the 'projectGAV' option");
        }
      }

      if (line.hasOption(JSON_REPORT)) {
        jsonReport = true;
      }

      if (line.hasOption(NO_COMPATIBILITY)) {
        noCompatibility = true;
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
    } catch (ParseException | ConsoleOptionsException e) {
      System.err.println("[ERROR] " + e.getMessage());
      printHelp(options);
      return false;
    }
    return true;
  }

  private void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("migration-assistant - Help", options);
  }

}
