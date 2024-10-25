/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report.html;

import com.mulesoft.tools.migration.report.AbstractReport;
import com.mulesoft.tools.migration.report.html.model.ApplicationReport;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.step.category.MigrationReport.*;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.*;

/**
 * Generates HTML Report
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HTMLReport extends AbstractReport {

  private static final String ASSETS_FOLDER = "assets";
  private static final String RESOURCES_FOLDER = "resources";
  private static final String STYLES_FOLDER = "styles";
  private static final String SCRIPTS_FOLDER = "js";

  private static final String BASE_TEMPLATE_FOLDER = "/templates";
  private static final String SUMMARY_TEMPLATE_FILE_NAME = "summary.ftl";
  private static final String RESOURCE_TEMPLATE_FILE_NAME = "resource.ftl";

  private static final String JQUERY_SCRIPT = "jquery-3.3.1.js";
  private static final String MULESOFT_STYLES = "mulesoft-styles.css";
  private static final String MULESOFT_ICON = "icons/004_logo.svg";
  private static final String MULESOFT_ICON_TTF = "fonts/muleicons.ttf";

  private ApplicationReport applicationReport;
  private ReportFileWriter reportFileWriter = new ReportFileWriter();
  private Configuration freemarkerConfig;
  private String runnerVersion;

  public HTMLReport(MigrationReport<ReportEntryModel> report, File reportDirectory, String runnerVersion) {
    super(report, reportDirectory);

    this.applicationReport = new ApplicationReport.ApplicationReportBuilder()
        .withReportEntries(report.getReportEntries()).build();
    this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_28);
    this.freemarkerConfig.setClassForTemplateLoading(this.getClass(), BASE_TEMPLATE_FOLDER);
    this.runnerVersion = runnerVersion;
  }

  public void printReport() {
    try {
      printSummary();
      printResources();
      addAssets();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setReportFileWriter(ReportFileWriter reportFileWriter) {
    this.reportFileWriter = reportFileWriter;
  }

  private void printSummary() throws IOException {
    StringWriter writer = null;
    try {
      Template summaryTemplate = getTemplate(SUMMARY_TEMPLATE_FILE_NAME);
      Map<String, Object> data = new HashMap<>();
      data.put("version", runnerVersion);
      data.put("applicationErrors", applicationReport.getErrorEntries());
      data.put("applicationWarnings", applicationReport.getWarningEntries());
      data.put("applicationInfo", applicationReport.getInfoEntries());
      data.put("applicationSummaryErrors", applicationReport.getSummaryErrorEntries());
      data.put("applicationSummaryWarnings", applicationReport.getSummaryWarningEntries());
      data.put("applicationSummaryInfo", applicationReport.getSummaryInfoEntries());
      data.put("componentsMigrated", String.format("%s/%s", report.getComponentSuccessCount(),
                                                   report.getComponentCount()));
      data.put("melExpressionsMigrated", String.format("%s/%s", report.getMelExpressionsSuccessCount(),
                                                       report.getMelExpressionsCount()));
      data.put("melExpressionLinesMigrated", String.format("%s/%s", report.getMelExpressionsSuccessLineCount(),
                                                           report.getMelExpressionsLineCount()));
      data.put("dwTransformationsMigrated", String.format("%s/%s", report.getDwTransformsSuccessCount(),
                                                          report.getDwTransformsCount()));
      data.put("dwTransformationLinesMigrated",
               String.format("%s/%s", report.getDwTransformsSuccessLineCount(),
                             report.getDwTransformsLineCount()));

      writer = new StringWriter();
      summaryTemplate.process(data, writer);
      reportFileWriter.writeToFile(new File(reportDirectory, "summary.html"), writer.getBuffer().toString());
    } catch (TemplateException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  private void printResources() throws IOException {
    generateResourceFiles(applicationReport.getErrorEntries(), ERROR);
    generateResourceFiles(applicationReport.getWarningEntries(), WARN);
    generateResourceFiles(applicationReport.getInfoEntries(), INFO);
  }

  private void generateResourceFiles(Map<String, Map<String, List<ReportEntryModel>>> entries, Level level) throws IOException {
    StringWriter writer = null;
    for (Map.Entry<String, Map<String, List<ReportEntryModel>>> entry : entries.entrySet()) {
      Integer fileCounter = 0;
      for (Map.Entry<String, List<ReportEntryModel>> fileEntry : entry.getValue().entrySet()) {
        try {
          if (fileEntry.getValue().size() > 0) {
            Template resourceTemplate = getTemplate(RESOURCE_TEMPLATE_FILE_NAME);

            Map<String, Object> data = new HashMap<>();
            data.put("version", runnerVersion);
            data.put("resource", Paths.get(entry.getKey()).getFileName().toString());
            data.put("description", fileEntry.getKey());
            data.put("docLinks", fileEntry.getValue().get(0).getDocumentationLinks());
            data.put("entries", fileEntry.getValue());

            String fileName = level.toString().toLowerCase() + "-" + Paths.get(entry.getKey()).getFileName().toString();

            writer = new StringWriter();
            resourceTemplate.process(data, writer);
            reportFileWriter.writeToFile(new File(reportDirectory.toPath().resolve(RESOURCES_FOLDER).toFile(),
                                                  reportFileWriter.getHtmlFileName(fileName, fileCounter)),
                                         writer.getBuffer().toString());
          }
        } catch (TemplateException e) {
          e.printStackTrace();
        } finally {
          IOUtils.closeQuietly(writer);
        }
        fileCounter++;
      }
    }
  }

  private Template getTemplate(String resourceTemplateFileName) throws IOException {
    return freemarkerConfig.getTemplate(resourceTemplateFileName, Locale.US);
  }

  private void addAssets() {
    addStyles();
    addScripts();
  }

  private void addStyles() {
    try {
      Path stylesPath = reportDirectory.toPath().resolve(ASSETS_FOLDER).resolve(STYLES_FOLDER);
      reportFileWriter.copyFile(MULESOFT_STYLES, stylesPath.resolve(MULESOFT_STYLES).toFile());
      reportFileWriter.copyFile(MULESOFT_ICON, stylesPath.resolve(MULESOFT_ICON).toFile());
      reportFileWriter.copyFile(MULESOFT_ICON_TTF, stylesPath.resolve(MULESOFT_ICON_TTF).toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void addScripts() {
    try {
      Path scriptsPath = reportDirectory.toPath().resolve(ASSETS_FOLDER).resolve(SCRIPTS_FOLDER);
      reportFileWriter.copyFile(JQUERY_SCRIPT, scriptsPath.resolve(JQUERY_SCRIPT).toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
