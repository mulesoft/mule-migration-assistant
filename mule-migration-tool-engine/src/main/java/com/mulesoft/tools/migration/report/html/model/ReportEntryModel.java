/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import org.jdom2.Element;
import org.jdom2.located.LocatedElement;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;

/**
 * Model for the HTML Report
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class ReportEntryModel {

  private Level level;
  private String element;
  private Integer lineNumber;
  private Integer columnNumber;
  private String message;
  private String filePath;
  private List<String> documentationLinks = new ArrayList<>();


  public ReportEntryModel(Level level, Element element, String message, String... documentationLinks) {
    this.level = level;
    this.element = escapeXml(new XMLOutputter(Format.getPrettyFormat()).outputString(element));
    this.message = message;
    this.filePath = element.getDocument().getBaseURI();

    for (String link : documentationLinks) {
      this.getDocumentationLinks().add(link);
    }

    if (element instanceof LocatedElement) {
      this.lineNumber = ((LocatedElement) element).getLine();
      this.columnNumber = ((LocatedElement) element).getColumn();
    }
  }

  public Level getLevel() {
    return level;
  }

  public String getElement() {
    return element;
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
