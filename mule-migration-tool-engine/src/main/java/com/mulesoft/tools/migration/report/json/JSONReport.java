/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates JSON Report
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JSONReport {


  private final File reportDirectory;
  private List<ReportEntryModel> reportEntries;
  private Path outputProject;


  public JSONReport(List<ReportEntryModel> reportEntries, File reportDirectory, Path outputProject) {
    this.reportEntries = reportEntries;
    this.outputProject = outputProject;
    checkNotNull(reportEntries, "Report Entries cannot be null");
    checkNotNull(reportDirectory, "Report directory cannot be null");
    this.reportDirectory = reportDirectory;
  }

  public void printReport() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    List<JSONReportModel> jsonReportModelList =
        reportEntries.stream()
            .map((re) -> JSONReportModel.fromReportModel(re, outputProject))
            .sorted(Comparator.comparing(JSONReportModel::getMessage))
            .sorted(Comparator.comparing(JSONReportModel::getColumnNumber))
            .sorted(Comparator.comparing(JSONReportModel::getLineNumber))
            .collect(Collectors.toList());
    String json = gson.toJson(jsonReportModelList);
    File file = new File(reportDirectory, "report.json");
    try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
      fileWriter.append(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * JSON Object model that represents the data exposed on the json document
   */
  static class JSONReportModel {

    private final MigrationReport.Level level;

    private final Integer lineNumber;
    private final Integer columnNumber;
    private final String message;
    private final String filePath;
    private final List<String> documentationLinks = new ArrayList<>();

    public JSONReportModel(MigrationReport.Level level, Integer lineNumber, Integer columnNumber, String message,
                           String filePath) {
      this.level = level;
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
      this.message = message;
      this.filePath = filePath;
    }

    public static JSONReportModel fromReportModel(ReportEntryModel rem, Path outputFolder) {
      String filePath = relativizePath(rem.getFilePath(), outputFolder);
      return new JSONReportModel(rem.getLevel(), rem.getLineNumber(), rem.getColumnNumber(), rem.getMessage(), filePath);
    }

    private static String relativizePath(String filePath, Path basePath) {
      if (filePath == null) {
        return "";
      }
      Path path = new File(filePath).toPath();
      if (path.startsWith(basePath)) {
        return basePath.relativize(path).toString();
      }
      String parentDomainBasePath = System.getProperty("parentDomainBasePath");
      if (parentDomainBasePath != null && path.startsWith(parentDomainBasePath)) {
        return "{parentDomainBasePath}:/" + Paths.get(parentDomainBasePath).relativize(path);
      }
      return filePath;
    }

    public MigrationReport.Level getLevel() {
      return level;
    }

    public Integer getLineNumber() {
      return lineNumber;
    }

    public Integer getColumnNumber() {
      return columnNumber;
    }

    public String getMessage() {
      return message;
    }

    public String getFilePath() {
      return filePath;
    }

    public List<String> getDocumentationLinks() {
      return documentationLinks;
    }
  }

}
