/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.printer;

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

  public static void printMigrationSummary(String reportPath, Long elapsedTime) {
    log("===============================================================================");
    log("MIGRATION SUCCESS");
    log("===============================================================================");
    log("Total time: " + String.format("%.3f", elapsedTime.floatValue() / 1000) + " s");
    log("Migration report: " + reportPath);
  }
}
