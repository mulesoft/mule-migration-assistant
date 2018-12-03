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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * It models the application on the report
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApplicationReport {

  private Map<String, Map<String, List<ReportEntryModel>>> errorEntries = new HashMap<>();
  private Map<String, Map<String, List<ReportEntryModel>>> warningEntries = new HashMap<>();
  private Map<String, Map<String, List<ReportEntryModel>>> infoEntries = new HashMap<>();

  protected ApplicationReport(Map<String, Map<String, List<ReportEntryModel>>> errorEntries,
                              Map<String, Map<String, List<ReportEntryModel>>> warningEntries,
                              Map<String, Map<String, List<ReportEntryModel>>> infoEntries) {
    this.errorEntries = errorEntries;
    this.warningEntries = warningEntries;
    this.infoEntries = infoEntries;
  }

  public Map<String, Map<String, List<ReportEntryModel>>> getErrorEntries() {
    return this.errorEntries;
  }

  public Map<String, Map<String, List<ReportEntryModel>>> getWarningEntries() {
    return this.warningEntries;
  }

  public Map<String, Map<String, List<ReportEntryModel>>> getInfoEntries() {
    return this.infoEntries;
  }

  public Map<String, Integer> getSummaryErrorEntries() {
    Map<String, Integer> summaryErrorEntries = new HashMap<>();
    Integer issuesCount = 0;

    for (Map.Entry<String, Map<String, List<ReportEntryModel>>> entry : errorEntries.entrySet()) {
      for (Map.Entry<String, List<ReportEntryModel>> e : entry.getValue().entrySet()) {
        issuesCount = issuesCount + e.getValue().size();
      }
      summaryErrorEntries.put(entry.getKey(), issuesCount);
      issuesCount = 0;
    }
    return summaryErrorEntries;
  }

  public Map<String, Integer> getSummaryWarningEntries() {
    Map<String, Integer> summaryWarningEntries = new HashMap<>();
    Integer issuesCount = 0;

    for (Map.Entry<String, Map<String, List<ReportEntryModel>>> entry : warningEntries.entrySet()) {
      for (Map.Entry<String, List<ReportEntryModel>> e : entry.getValue().entrySet()) {
        issuesCount = issuesCount + e.getValue().size();
      }
      summaryWarningEntries.put(entry.getKey(), issuesCount);
      issuesCount = 0;
    }
    return summaryWarningEntries;
  }

  public Map<String, Integer> getSummaryInfoEntries() {
    Map<String, Integer> summaryWarningEntries = new HashMap<>();
    Integer issuesCount = 0;

    for (Map.Entry<String, Map<String, List<ReportEntryModel>>> entry : infoEntries.entrySet()) {
      for (Map.Entry<String, List<ReportEntryModel>> e : entry.getValue().entrySet()) {
        issuesCount = issuesCount + e.getValue().size();
      }
      summaryWarningEntries.put(entry.getKey(), issuesCount);
      issuesCount = 0;
    }
    return summaryWarningEntries;
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
      Map<String, Map<String, List<ReportEntryModel>>> errorEntries = getEntries(Level.ERROR);
      Map<String, Map<String, List<ReportEntryModel>>> warningEntries = getEntries(Level.WARN);
      Map<String, Map<String, List<ReportEntryModel>>> infoEntries = getEntries(Level.INFO);
      return new ApplicationReport(errorEntries, warningEntries, infoEntries);
    }

    private Map<String, Map<String, List<ReportEntryModel>>> getEntries(Level level) {
      Map<String, Map<String, List<ReportEntryModel>>> resources = new HashMap<>();
      reportEntries.forEach(e -> {
        if (e.getLevel().equals(level)) {
          String fileName = e.getFilePath() != null ? Paths.get(e.getFilePath()).getFileName().toString() : "misc";
          Map<String, List<ReportEntryModel>> entries = resources.computeIfAbsent(fileName, k -> new LinkedHashMap<>());
          entries.computeIfAbsent(e.getMessage(), f -> new ArrayList<>()).add(e);
        }
      });
      return resources;
    }

  }
}
