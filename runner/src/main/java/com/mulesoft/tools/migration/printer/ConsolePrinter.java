/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
