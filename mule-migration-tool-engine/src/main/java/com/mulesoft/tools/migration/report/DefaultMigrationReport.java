/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.report.html.model.ReportEntryModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Comment;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a {@link MigrationReport}.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DefaultMigrationReport implements MigrationReport {

  private XMLOutputter outp = new XMLOutputter();
  private List<ReportEntryModel> reportEntries = new ArrayList<>();

  @Override
  public void report(Level level, Element element, Element elementToComment, String message, String... documentationLinks) {
    int i = 0;

    elementToComment.addContent(i++, new Comment("Migration " + level.name() + ": " + message));
    elementToComment.addContent(i++, new Comment("    For more information refer to:"));

    for (String link : documentationLinks) {
      elementToComment.addContent(i++, new Comment("        * " + link));
    }

    if (element != elementToComment) {
      elementToComment.addContent(i++, new Comment(outp.outputString(element)));
    }
    reportEntries.add(new ReportEntryModel(level, elementToComment, message, documentationLinks));
  }

  public List<ReportEntryModel> getReportEntries() {
    return this.reportEntries;
  }

}
