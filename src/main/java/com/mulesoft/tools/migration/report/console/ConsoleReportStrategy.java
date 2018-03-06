/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.console;

import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.engine.task.DefaultMigrationTask;
import com.mulesoft.tools.migration.engine.step.DefaultMigrationStep;

/**
 * I know how to report data through a Console
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ConsoleReportStrategy implements ReportingStrategy {

  @Override
  public void log(String message, ReportCategory reportCategory, String filePath, DefaultMigrationTask task,
                  DefaultMigrationStep step) {
    System.out.println(reportCategory.getDescription() + " : " + message);
  }
}
