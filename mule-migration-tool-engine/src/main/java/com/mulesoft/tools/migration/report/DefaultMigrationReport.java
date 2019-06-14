/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static java.util.Collections.emptyList;
import static java.util.Collections.list;

import com.mulesoft.tools.migration.exception.MigrationAbortException;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Comment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of a {@link MigrationReport}.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DefaultMigrationReport implements MigrationReport {

  private transient Map<String, Map<String, Map<String, Object>>> possibleEntries;

  private transient XMLOutputter outp = new XMLOutputter();
  private final Set<ReportEntryModel> reportEntries = new HashSet<>();

  private String projectType;
  private String projectName;

  private double successfulMigrationRatio;
  private double errorMigrationRatio;
  private int processedElements;


  public DefaultMigrationReport() {
    possibleEntries = new HashMap<>();
    try {
      for (URL reportYamlUrl : list(DefaultMigrationReport.class.getClassLoader().getResources("report.yaml"))) {
        try (InputStream yamlStream = reportYamlUrl.openStream()) {
          possibleEntries.putAll(new Yaml().loadAs(yamlStream, Map.class));
        }
      }
    } catch (IOException e) {
      throw new MigrationAbortException("Couldn't load report entries definitions.", e);
    }
  }

  @Override
  public void initialize(ProjectType projectType, String projectName) {
    this.projectType = projectType.name();
    this.projectName = projectName;
  }

  @Override
  public void report(String entryKey, Element element, Element elementToComment, String... messageParams) {
    final String[] splitEntryKey = entryKey.split("\\.");

    final Map<String, Object> entryData = possibleEntries.get(splitEntryKey[0]).get(splitEntryKey[1]);

    final Level level = Level.valueOf((String) entryData.get("type"));
    String message = (String) entryData.get("message");
    for (String messageParam : messageParams) {
      message = message.replaceFirst("\\{\\w*\\}", messageParam);
    }

    final List<String> docLinks = entryData.get("docLinks") != null ? (List<String>) entryData.get("docLinks") : emptyList();
    report(level, element, elementToComment, message, docLinks.toArray(new String[docLinks.size()]));
  }

  @Override
  public void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks) {
    int i = 0;

    ReportEntryModel reportEntry;

    if (elementToComment.getDocument() != null) {
      reportEntry = new ReportEntryModel(level, elementToComment, message, documentationLinks);
    } else {
      reportEntry = new ReportEntryModel(level, elementToComment, message, element.getDocument(), documentationLinks);
    }

    if (reportEntries.add(reportEntry)) {
      if (elementToComment != null) {
        elementToComment.addContent(i++, new Comment("Migration " + level.name() + ": " + message));

        if (documentationLinks.length > 0) {
          elementToComment.addContent(i++, new Comment("    For more information refer to:"));

          for (String link : documentationLinks) {
            elementToComment.addContent(i++, new Comment("        * " + link));
          }
        }

        if (element != elementToComment) {
          elementToComment.addContent(i++, new Comment(outp.outputString(element)));
        }
      }
    }
  }

  @Override
  public void addProcessedElements(int processedElements) {
    this.processedElements += processedElements;
    this.successfulMigrationRatio = (1.0 * (this.processedElements - reportEntries.stream()
        .filter(re -> re.getElement() != null && !"compatibility".equals(re.getElement().getNamespacePrefix()))
        .map(re -> re.getElement()).distinct().count())) / this.processedElements;
    this.errorMigrationRatio = (1.0 * reportEntries.stream()
        .filter(re -> re.getElement() != null && ERROR.equals(re.getLevel()))
        .map(re -> re.getElement()).distinct().count()) / this.processedElements;
  }

  @Override
  public void updateReportEntryFilePath(Path oldFileName, Path newFileName) {
    reportEntries.stream().filter(e -> e.getFilePath().equals(oldFileName.toString()))
        .forEach(r -> r.setFilePath(newFileName.toString()));
  }

  public String getProjectType() {
    return projectType;
  }

  public String getProjectName() {
    return projectName;
  }

  @Override
  public List<ReportEntryModel> getReportEntries() {
    return new ArrayList<>(this.reportEntries);
  }

  public double getSuccessfulMigrationRatio() {
    return successfulMigrationRatio;
  }

  public double getErrorMigrationRatio() {
    return errorMigrationRatio;
  }
}
