/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration;

import com.mulesoft.tools.migration.exception.ConsoleOptionsException;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.html.HTMLReportStrategy;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mulesoft.tools.migration.MigrationRunner.MigrationConsoleOptions.*;

public class MigrationRunner {

    private List<String> files;
    private String filesDir;
    private String migrationConfigFile;
    private String migrationConfigDir;
    private Boolean backup;
    private Boolean onErrorStop;
    private ReportingStrategy reportingStrategy;

    public static void main(String args[]) throws Exception {
        MigrationRunner migrationRunner = new MigrationRunner();
        migrationRunner.initializeOptions(args);

        MigrationJob job = new MigrationJob();
        job.setBackUpProfile(migrationRunner.backup);
        job.setOnErrorStop(migrationRunner.onErrorStop);
        job.setDocuments(migrationRunner.files);
        job.setFilesDir(migrationRunner.filesDir);
        job.setConfigFilePath(migrationRunner.migrationConfigFile);
        job.setConfigFileDir(migrationRunner.migrationConfigDir);
        job.setReportingStrategy(migrationRunner.reportingStrategy);
        job.execute();
    }

    /**
     * Initialises the console options with Apache Command Line
     * @param args
     */
    private void initializeOptions(String[] args) {

        Options options = new Options();

        options.addOption(MIGRATION_CONFIG_FILE,true,"Migration config file (json) containing all the rules and step" );
        options.addOption(MIGRATION_CONFIG_DIR,true,"Migration config root directory containing all the json files with the rules configurations" );
        options.addOption(FILES,true,"List of paths separated by ';' example: path1;path2...etc");
        options.addOption(FILES_DIR,true,"Root directory of the files to be migrated");
        options.addOption(BACKUP,true,"Flag to determine if you want a backup of your original files");
        options.addOption(HELP,false,"Shows the help");
        options.addOption(REPORT,false,"Reporting strategy (default: console)");
        options.addOption(ON_ERROR_STOP,false,"Defines if the tool should stop when an error happens (default:true)");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine line = parser.parse(options, args);

            if(line.hasOption(MIGRATION_CONFIG_FILE) && !line.hasOption(MIGRATION_CONFIG_DIR)) {
                this.migrationConfigFile = line.getOptionValue(MIGRATION_CONFIG_FILE);
            } else if (!line.hasOption(MIGRATION_CONFIG_FILE) && line.hasOption(MIGRATION_CONFIG_DIR))  {
                this.migrationConfigDir = line.getOptionValue(MIGRATION_CONFIG_DIR);
            } else {
                throw new ConsoleOptionsException("You must specify a migration config file OR a config dir");
            }

            if(line.hasOption(FILES) && !line.hasOption(FILES_DIR)) {
                this.files = new ArrayList<>(Arrays.asList(line.getOptionValue(FILES).split(";")));
            } else if (!line.hasOption(FILES) && line.hasOption(FILES_DIR)) {
                this.filesDir = line.getOptionValue(FILES_DIR);
            } else {
                throw new ConsoleOptionsException("You must specify a root directory of the files to be migrated OR a list " +
                        "of paths separated by ';' example: path1;path2...etc");
            }

            if(line.hasOption(BACKUP)) {
                this.backup = Boolean.parseBoolean(line.getOptionValue(BACKUP));
            } else {
                this.backup = Boolean.FALSE;
            }

            if(line.hasOption(REPORT)) {
                if(line.getOptionValue(REPORT).equals("html")) {
                    this.reportingStrategy = new HTMLReportStrategy();
                } else {
                    this.reportingStrategy = new ConsoleReportStrategy();
                }
            }else {
                this.reportingStrategy = new ConsoleReportStrategy();
            }

            if(line.hasOption(ON_ERROR_STOP)) {
                this.onErrorStop = Boolean.parseBoolean(line.getOptionValue(ON_ERROR_STOP));
            } else {
                this.onErrorStop = Boolean.TRUE;
            }

            if(line.hasOption(HELP)) {
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

    static class MigrationConsoleOptions {
        public final static String MIGRATION_CONFIG_FILE = "migrationConfigFile";
        public final static String MIGRATION_CONFIG_DIR= "migrationConfigDir";
        public final static String FILES= "files";
        public final static String FILES_DIR= "filesDir";
        public final static String BACKUP= "backup";
        public final static String REPORT= "report";
        public final static String ON_ERROR_STOP= "onErrorStop";
        public final static String HELP= "help";
    }
}