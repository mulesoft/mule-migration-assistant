/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.text.StringEscapeUtils.escapeXml11;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.step.category.MigrationReport.Level;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;
import org.jdom2.output.support.XMLOutputProcessor;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model for the HTML Report
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ReportEntryModel {

  private Level level;
  private String elementContent;
  private transient Element element;
  private Integer lineNumber = 0;
  private Integer columnNumber = 0;
  private String message;
  private String filePath;
  private List<String> documentationLinks = new ArrayList<>();


  public ReportEntryModel(Level level, Element element, String message, String... documentationLinks) {
    this.level = level;
    this.elementContent = escapeXml11(domElementToString(element));
    this.element = element;
    this.message = message;
    try {
      this.filePath = new File(new URI(element.getDocument().getBaseURI())).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new RuntimeException("Report Generation Error - Fail to get file: " + element.getDocument().getBaseURI(), e);
    }
    this.documentationLinks.addAll(asList(documentationLinks));
  }

  public void setElementLocation() throws Exception {
    try {
      SAXBuilder saxBuilder = new SAXBuilder();
      saxBuilder.setJDOMFactory(new LocatedJDOMFactory());
      Document document = saxBuilder.build(Paths.get(filePath).toFile());
      setElementLocation(document);
    } catch (Exception ex) {
      throw new MigrationJobException("Failed to obtain new element location.", ex.getCause());
    }
  }

  private void setElementLocation(Document document) {
    String xpathExpression = "";
    Element currentElement = element;

    while (currentElement != element.getDocument().getRootElement()) {
      xpathExpression =
          "/*[" + (1 + currentElement.getParentElement().getChildren().indexOf(currentElement)) + "]" + xpathExpression;
      currentElement = currentElement.getParentElement();
    }

    xpathExpression = "/*" + xpathExpression;

    List<Element> elements =
        XPathFactory.instance()
            .compile(xpathExpression, Filters.element(), null,
                     document.getRootElement().getAdditionalNamespaces())
            .evaluate(document);
    if (elements.size() > 0) {
      this.lineNumber = ((LocatedElement) elements.get(0)).getLine();
      this.columnNumber = ((LocatedElement) elements.get(0)).getColumn();
    }
  }

  private String domElementToString(Element element) {
    Format format = Format.getPrettyFormat();
    format.setTextMode(Format.TextMode.NORMALIZE);
    format.setEncoding("UTF-8");
    format.setIndent("    ");

    XMLOutputter xmlOut = new XMLOutputter(noNamespaces);
    xmlOut.setFormat(format);
    return xmlOut.outputString(maskAttributesRecursively(element.clone()));
  }

  protected Element maskAttributesRecursively(Element element) {
    maskAttributes(element);
    element.getChildren().forEach(c -> {
      maskAttributes(c);
      maskAttributesRecursively(c);
    });
    return element;
  }

  protected void maskAttributes(Element element) {
    element.getAttributes().forEach(att -> {
      if (containsIgnoreCase(att.getName(), "password") || containsIgnoreCase(att.getName(), "secret")) {
        att.setValue("****");
      }
    });
  }

  private static final XMLOutputProcessor noNamespaces = new AbstractXMLOutputProcessor() {

    @Override
    protected void printNamespace(final Writer out, final FormatStack formatStack,
                                  final Namespace ns)
        throws IOException {
      // do nothing with printing Namespaces....
    }
  };

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

  public Element getElement() {
    return element;
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

  @Override
  public int hashCode() {
    return Objects.hash(getLevel(), getElement(), getMessage(), getDocumentationLinks());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    ReportEntryModel that = (ReportEntryModel) obj;
    return Objects.equals(getLevel(), that.getLevel()) &&
        Objects.equals(getElement(), that.getElement()) &&
        Objects.equals(getMessage(), that.getMessage()) &&
        Objects.equals(getDocumentationLinks(), that.getDocumentationLinks());
  }

}
