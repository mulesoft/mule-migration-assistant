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
package com.mulesoft.tools.migration.report.html;

import com.mulesoft.tools.migration.report.html.model.ApplicationReport;
import com.mulesoft.tools.migration.report.html.model.ApplicationReport.ApplicationReportBuilder;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.mulesoft.tools.migration.step.category.MigrationReport.*;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.*;

/**
 * Generates HTML Report
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HTMLReport {

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

  private final File reportDirectory;
  private ApplicationReport applicationReport;
  private ReportFileWriter reportFileWriter = new ReportFileWriter();
  private Configuration freemarkerConfig;
  private String runnerVersion;

  public HTMLReport(List<ReportEntryModel> reportEntries, File reportDirectory, String runnerVersion) {
    checkNotNull(reportEntries, "Report Entries cannot be null");
    checkNotNull(reportDirectory, "Report directory cannot be null");
    this.applicationReport = new ApplicationReportBuilder().withReportEntries(reportEntries).build();
    this.reportDirectory = reportDirectory;
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
