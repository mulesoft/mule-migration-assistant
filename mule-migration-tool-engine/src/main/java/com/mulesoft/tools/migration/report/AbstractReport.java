package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

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
