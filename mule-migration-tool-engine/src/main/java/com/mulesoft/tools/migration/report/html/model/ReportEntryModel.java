/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathFactory;
import org.jdom2.xpath.XPathHelper;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getAdditionalNamespaces;
import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;

/**
 * Model for the HTML Report
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class ReportEntryModel {

  private Level level;
  private String elementContent;
  private Element element;
  private Integer lineNumber = 0;
  private Integer columnNumber = 0;
  private String message;
  private String filePath;
  private List<String> documentationLinks = new ArrayList<>();


  public ReportEntryModel(Level level, Element element, String message, String... documentationLinks) {
    this.level = level;
    this.elementContent = escapeXml(new XMLOutputter(Format.getPrettyFormat()).outputString(element));
    this.element = element;
    this.message = message;
    this.filePath = element.getDocument().getBaseURI();

    for (String link : documentationLinks) {
      this.getDocumentationLinks().add(link);
    }
  }

  public void setElementLocation() throws Exception {
    try {
      SAXBuilder saxBuilder = new SAXBuilder();
      saxBuilder.setJDOMFactory(new LocatedJDOMFactory());
      Document document = saxBuilder.build(Paths.get(URI.create(filePath)).toFile());
      setElementLocation(document);
    } catch (Exception ex) {
      throw new MigrationJobException("Failed to obtain new element location.", ex.getCause());
    }
  }

  private void setElementLocation(Document document) {
    String xpathExpression = XPathHelper.getAbsolutePath(element);
    List<Element> elements = XPathFactory.instance().compile(xpathExpression, Filters.element(), null, getAdditionalNamespaces())
        .evaluate(document);
    if (elements.size() > 0) {
      this.lineNumber = ((LocatedElement) elements.get(0)).getLine();
      this.columnNumber = ((LocatedElement) elements.get(0)).getColumn();
    }
  }
  
  public Level getLevel() {
    return level;
  }

  public String getElementContent() {
    return elementContent;
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
