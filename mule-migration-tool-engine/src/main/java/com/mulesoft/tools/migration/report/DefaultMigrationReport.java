/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Comment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of a {@link MigrationReport}.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DefaultMigrationReport implements MigrationReport {

  private transient XMLOutputter outp = new XMLOutputter();
  private transient Set<ReportEntryModel> reportEntries = new HashSet<>();

  private String projectType;
  private String projectName;

  private double successfulMigrationRatio;
  private double errorMigrationRatio;
  private int processedElements;

  @Override
  public void initialize(ProjectType projectType, String projectName) {
    this.projectType = projectType.name();
    this.projectName = projectName;
  }

  @Override
  public void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks) {
    int i = 0;

    ReportEntryModel reportEntry = new ReportEntryModel(level, elementToComment, message, documentationLinks);

    if (reportEntries.add(reportEntry)) {

      elementToComment.addContent(i++, new Comment("Migration " + level.name() + ": " + message));
      elementToComment.addContent(i++, new Comment("    For more information refer to:"));

      for (String link : documentationLinks) {
        elementToComment.addContent(i++, new Comment("        * " + link));
      }

      if (element != elementToComment) {
        elementToComment.addContent(i++, new Comment(outp.outputString(element)));
      }
    }
  }

  @Override
  public void addProcessedElements(int processedElements) {
    this.processedElements += processedElements;
    this.successfulMigrationRatio = (1.0 * (this.processedElements - reportEntries.stream()
        .filter(re -> !"compatibility".equals(re.getElement().getNamespacePrefix()))
        .map(re -> re.getElement()).distinct().count())) / this.processedElements;
    this.errorMigrationRatio = (1.0 * reportEntries.stream()
        .filter(re -> ERROR.equals(re.getLevel()))
        .map(re -> re.getElement()).distinct().count()) / this.processedElements;
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
