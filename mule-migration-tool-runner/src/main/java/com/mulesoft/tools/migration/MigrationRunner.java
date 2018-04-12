/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static java.util.Collections.singletonList;

import com.mulesoft.tools.migration.engine.MigrationJob;
import com.mulesoft.tools.migration.engine.MigrationJob.MigrationJobBuilder;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.exception.ConsoleOptionsException;
import com.mulesoft.tools.migration.library.mule.tasks.PreprocessMuleApplication;
import com.mulesoft.tools.migration.report.DefaultMigrationReport;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.html.HTMLReportStrategy;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import com.mulesoft.tools.migration.task.Version;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Paths;

/**
 * Base entry point to run {@link AbstractMigrationTask}s
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationRunner {

  private final static String HELP = "help";

  private final static String REPORT = "report";

  private final static String PROJECT_BASE_PATH = "projectBasePath";
  private final static String DESTINATION_PROJECT_BASE_PATH = "destinationProjectBasePath";
  private final static String MIGRATION_CONFIGURATION_PATH = "migrationConfigurationPath";
  public static final Version MULE_3_VERSION = new Version.VersionBuilder().withMajor("3").build();
  public static final Version MULE_4_VERSION = new Version.VersionBuilder().withMajor("4").build();
  public static final ProjectType OUTPUT_PROJECT_TYPE = MULE_FOUR_APPLICATION;

  private String projectBasePath;
  private String destinationProjectBasePath;
  private String migrationConfigurationPath;

  private ReportingStrategy reportingStrategy;

  public static void main(String args[]) throws Exception {
    MigrationRunner migrationRunner = new MigrationRunner();
    migrationRunner.initializeOptions(args);

    MigrationJob job = migrationRunner.buildMigrationJob();
    job.execute(new DefaultMigrationReport());
  }

  private MigrationJob buildMigrationJob() {
    MigrationJobBuilder builder = new MigrationJobBuilder()
        .withProject(Paths.get(projectBasePath))
        .withOutputProject(Paths.get(destinationProjectBasePath))
        .withReportingStrategy(reportingStrategy)
        .withInputVersion(MULE_3_VERSION)
        .withOuputVersion(MULE_4_VERSION)
        .withOutputProjectType(OUTPUT_PROJECT_TYPE);
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
    options.addOption(MIGRATION_CONFIGURATION_PATH, true,
                      "Migration configuration path containing all the json files with the rules configurations");
    options.addOption(PROJECT_BASE_PATH, true, "Base directory of the project  to be migrated");
    options.addOption(DESTINATION_PROJECT_BASE_PATH, true, "Base directory of the migrated project");
    options.addOption(REPORT, false, "Reporting strategy (default: console)");

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine line = parser.parse(options, args);

      if (line.hasOption(MIGRATION_CONFIGURATION_PATH)) {
        this.migrationConfigurationPath = line.getOptionValue(MIGRATION_CONFIGURATION_PATH);
      } else {
        throw new ConsoleOptionsException("You must specify a migration configuration path");
      }

      if (line.hasOption(PROJECT_BASE_PATH)) {
        this.projectBasePath = line.getOptionValue(PROJECT_BASE_PATH);
      } else {
        throw new ConsoleOptionsException("You must specify a project base path of the files to be migrated");
      }

      if (line.hasOption(DESTINATION_PROJECT_BASE_PATH)) {
        this.destinationProjectBasePath = line.getOptionValue(DESTINATION_PROJECT_BASE_PATH);
      } else {
        throw new ConsoleOptionsException("You must specify a destination project base path");
      }


      if (line.hasOption(REPORT)) {
        if (line.getOptionValue(REPORT).equals("html")) {
          this.reportingStrategy = new HTMLReportStrategy();
        } else {
          this.reportingStrategy = new ConsoleReportStrategy();
        }
      } else {
        this.reportingStrategy = new ConsoleReportStrategy();
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
