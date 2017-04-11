package com.mulesoft.tools.migration;

import com.mulesoft.tools.migration.exception.ConsoleOptionsException;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;

public class MigrationRunner {

    private ArrayList<String> files;
    private String migrationConfigFile;
    private Boolean backup;

    public static void main(String args[]) throws Exception {
        MigrationRunner migrationRunner = new MigrationRunner();
        migrationRunner.initializeOptions(args);

        MigrationJob job = new MigrationJob();
        job.setBackUpProfile(migrationRunner.backup);
        job.setDocuments(migrationRunner.files);
        job.setConfigFilePath(migrationRunner.migrationConfigFile);

        job.execute();
    }

    /**
     * Initialises the console options with Apache Command Line
     * @param args
     */
    private void initializeOptions(String[] args) {

        Options options = new Options();

        options.addOption("migrationConfigFile",true,"Migration config file (json) containing all the rules and steps" );
        options.addOption("files",true,"List of paths separated by ';' example: path1;path2...etc");
        options.addOption("backup",true,"Flag to determine if you want a backup of your original files");
        options.addOption("help",false,"Shows the help");

        try {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(options, args);

            if(line.hasOption( "migrationConfigFile" )) {
                this.migrationConfigFile = line.getOptionValue( "migrationConfigFile" );
            }else{
                throw new ConsoleOptionsException("You must specify a migration config file");
            }

            if(line.hasOption( "files" )) {
                this.files = new ArrayList<>(Arrays.asList(line.getOptionValue( "files" ).split(";")));
            }else{
                throw new ConsoleOptionsException("You must specify a file path or a list of paths separated by ';' example: path1;path2...etc");
            }

            if(line.hasOption( "backup" )) {
                this.backup = Boolean.parseBoolean(line.getOptionValue( "backup" ));
            }else{
                this.backup = Boolean.FALSE;
            }

            if(line.hasOption( "help" )) {
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