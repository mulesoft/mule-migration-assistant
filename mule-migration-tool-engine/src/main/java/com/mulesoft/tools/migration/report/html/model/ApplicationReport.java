/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import com.mulesoft.tools.migration.step.category.MigrationReport.Level;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It models the application on the report
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApplicationReport {

  private Map<String, List<ReportEntryModel>> errorEntries = new HashMap<>();
  private Map<String, List<ReportEntryModel>> warningEntries = new HashMap<>();

  protected ApplicationReport(Map<String, List<ReportEntryModel>> errorEntries,
                              Map<String, List<ReportEntryModel>> warningEntries) {
    this.errorEntries = errorEntries;
    this.warningEntries = warningEntries;
  }

  public Map<String, List<ReportEntryModel>> getErrorEntries() {
    return this.errorEntries;
  }

  public Map<String, List<ReportEntryModel>> getWarningEntries() {
    return this.warningEntries;
  }

  /**
   * It represent the builder to obtain a {@link ApplicationReport}
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class ApplicationReportBuilder {

    private List<ReportEntryModel> reportEntries;

    public ApplicationReportBuilder withReportEntries(List<ReportEntryModel> reportEntries) {
      this.reportEntries = reportEntries;
      return this;
    }

    public ApplicationReport build() {
      Map<String, List<ReportEntryModel>> errorEntries = getEntries(Level.ERROR);
      Map<String, List<ReportEntryModel>> warningEntries = getEntries(Level.WARN);
      return new ApplicationReport(errorEntries, warningEntries);
    }

    private Map<String, List<ReportEntryModel>> getEntries(Level level) {
      Map<String, List<ReportEntryModel>> resources = new HashMap<>();
      reportEntries.forEach(e -> {
        if (e.getLevel().equals(level)) {
          String fileName = Paths.get(e.getFilePath()).getFileName().toString();
          resources.computeIfAbsent(fileName, k -> new ArrayList<>()).add(e);
        }
      });
      return resources;
    }
  }
}
