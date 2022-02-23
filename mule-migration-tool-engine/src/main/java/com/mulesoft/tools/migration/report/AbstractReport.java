/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract class for migration reports
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractReport {

  protected final File reportDirectory;
  protected final MigrationReport<ReportEntryModel> report;

  public AbstractReport(MigrationReport<ReportEntryModel> report, File reportDirectory) {
    this.report = report;
    checkNotNull(reportDirectory, "Report directory cannot be null");
    checkNotNull(report.getReportEntries(), "Report Entries cannot be null");
    this.reportDirectory = reportDirectory;
  }

}
