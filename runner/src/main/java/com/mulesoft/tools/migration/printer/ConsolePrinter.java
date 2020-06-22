/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.printer;

import static java.lang.String.format;

import com.mulesoft.tools.migration.report.DefaultMigrationReport;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Prints output messages on console.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ConsolePrinter {

  public static void log(String message) {
    System.out.println(message);
  }

  public static void printMigrationSummary(String reportPath, Long elapsedTime, DefaultMigrationReport report) {
    log("===============================================================================");
    log("MIGRATION ASSISTANT RUN SUCCESSFULLY");
    log("===============================================================================");
    log("Total time: " + format("%.3f", elapsedTime.floatValue() / 1000) + " s");
    log("Migration report: " + reportPath);
  }

  public static void printMigrationError(Exception exception, Long elapsedTime) {
    log("===============================================================================");
    log("MIGRATION FAILED");
    log("===============================================================================");
    log("Total time: " + format("%.3f", elapsedTime.floatValue() / 1000) + " s");
    log("Exception: " + exception.getMessage());
    StringWriter exceptionWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(exceptionWriter));
    log(exceptionWriter.toString());
    log("===============================================================================");
  }
}
