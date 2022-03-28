/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mulesoft.tools.migration.report.AbstractReport;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.ComponentMigrationStatus;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.report.DefaultMigrationReport.getComponentKeyStatic;

import org.jdom2.Element;

/**
 * Generates JSON Report
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JSONReport extends AbstractReport {

  private final Path outputProject;

  public JSONReport(MigrationReport<ReportEntryModel> report, File reportDirectory, Path outputProject) {
    super(report, reportDirectory);
    this.outputProject = outputProject;
  }

  public void printReport() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    JSONReportModel jsonReportModel = new JSONReportModel(report, outputProject);
    String json = gson.toJson(jsonReportModel);
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

    private final String projectType;
    private final String projectName;

    private final List<String> connectorsMigrated;
    private final Integer numberOfMuleComponents;
    private final Integer numberOfMuleComponentsMigrated;
    private final Map<String, ComponentMigrationStatus> componentDetails;
    private final Integer numberOfMELExpressions;
    private final Integer numberOfMELExpressionsMigrated;
    private final Integer numberOfMELExpressionLines;
    private final Integer numberOfMELExpressionLinesMigrated;
    private final Integer numberOfDWTransformations;
    private final Integer numberOfDWTransformationsMigrated;
    private final Integer numberOfDWTransformationLines;
    private final Integer numberOfDWTransformationLinesMigrated;
    private final List<JSONReportEntryModel> detailedMessages;

    public JSONReportModel(MigrationReport<ReportEntryModel> report, Path outputProject) {
      projectType = report.getProjectType();
      projectName = report.getProjectName();

      connectorsMigrated = report.getConnectorNames();
      numberOfMuleComponentsMigrated = report.getComponentSuccessCount();
      numberOfMuleComponents = report.getComponentFailureCount() + numberOfMuleComponentsMigrated;
      componentDetails = report.getComponents();

      numberOfMELExpressionsMigrated = report.getMelExpressionsSuccessCount();
      numberOfMELExpressions = report.getMelExpressionsCount();
      numberOfMELExpressionLinesMigrated = report.getMelExpressionsSuccessLineCount();
      numberOfMELExpressionLines = report.getMelExpressionsLineCount();
      numberOfDWTransformationsMigrated = report.getDwTransformsSuccessCount();
      numberOfDWTransformations = report.getDwTransformsCount();
      numberOfDWTransformationLinesMigrated = report.getDwTransformsSuccessLineCount();
      numberOfDWTransformationLines = report.getDwTransformsLineCount();

      detailedMessages = report.getReportEntries().stream()
          .map((re) -> JSONReportEntryModel.fromReportModel(re, outputProject))
          .sorted(Comparator.comparing(JSONReportEntryModel::getMessage))
          .sorted(Comparator.comparing(JSONReportEntryModel::getColumnNumber))
          .sorted(Comparator.comparing(JSONReportEntryModel::getLineNumber))
          .collect(Collectors.toList());
    }

    public String getProjectType() {
      return projectType;
    }

    public String getProjectName() {
      return projectName;
    }

    public List<String> getConnectorsMigrated() {
      return connectorsMigrated;
    }

    public Integer getNumberOfMuleComponents() {
      return numberOfMuleComponents;
    }

    public Integer getNumberOfMuleComponentsMigrated() {
      return numberOfMuleComponentsMigrated;
    }

    public Map<String, ComponentMigrationStatus> getComponentDetails() {
      return componentDetails;
    }

    public Integer getNumberOfMELExpressions() {
      return numberOfMELExpressions;
    }

    public Integer getNumberOfMELExpressionsMigrated() {
      return numberOfMELExpressionsMigrated;
    }

    public Integer getNumberOfMELExpressionLines() {
      return numberOfMELExpressionLines;
    }

    public Integer getNumberOfMELExpressionLinesMigrated() {
      return numberOfMELExpressionLinesMigrated;
    }

    public Integer getNumberOfDWTransformations() {
      return numberOfDWTransformations;
    }

    public Integer getNumberOfDWTransformationsMigrated() {
      return numberOfDWTransformationsMigrated;
    }

    public Integer getNumberOfDWTransformationLines() {
      return numberOfDWTransformationLines;
    }

    public Integer getNumberOfDWTransformationLinesMigrated() {
      return numberOfDWTransformationLinesMigrated;
    }

    public List<JSONReportEntryModel> getDetailedMessages() {
      return detailedMessages;
    }

  }

  static class JSONReportEntryModel {

    private final MigrationReport.Level level;
    private final String key;
    private final String component;
    private final Integer lineNumber;
    private final Integer columnNumber;
    private final String message;
    private final String filePath;
    private final List<String> documentationLinks = new ArrayList<>();

    private JSONReportEntryModel(String key, MigrationReport.Level level, Element component, Integer lineNumber,
                                 Integer columnNumber, String message,
                                 String filePath) {
      this.key = key;
      this.level = level;
      this.component = component != null ? getComponentKeyStatic(component) : "UNKNOWN";
      this.lineNumber = lineNumber;
      this.columnNumber = columnNumber;
      this.message = message;
      this.filePath = filePath;
    }

    public static JSONReportEntryModel fromReportModel(ReportEntryModel rem, Path outputFolder) {
      String filePath = relativizePath(rem.getFilePath(), outputFolder);
      return new JSONReportEntryModel(rem.getKey(), rem.getLevel(), rem.getElement(), rem.getLineNumber(), rem.getColumnNumber(),
                                      rem.getMessage(),
                                      filePath);
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

    public String getKey() {
      return key;
    }

    public MigrationReport.Level getLevel() {
      return level;
    }

    public String getComponent() {
      return component;
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
