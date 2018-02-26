/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.structure.BasicProject.getFiles;
import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.mulesoft.tools.migration.project.structure.mule.three.MuleApplicationProject;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Represent the application to be migrated
 * 
 * @author Mulesoft Inc.
 */
public class ApplicationModel {

  private Map<Path, Document> applicationDocuments;

  protected ApplicationModel(Map<Path, Document> applicationDocuments) {
    this.applicationDocuments = applicationDocuments;
  }

  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  public List<Element> getNodes(String xpathExpression) {
    checkArgument(StringUtils.isNotBlank(xpathExpression), "The Xpath Expression must not be null nor empty");

    List<Element> nodes = new ArrayList<>();
    for (Document doc : applicationDocuments.values()) {
      nodes.addAll(getXPathExpression(xpathExpression, doc).evaluate(doc));
    }

    return nodes;
  }

  public void addNameSpace(String prefix, String uri) {
    for (Document doc : applicationDocuments.values()) {
      addNameSpace(prefix, uri, doc);
    }
  }

  public void addNameSpace(String prefix, String uri, Document doc) {
    doc.getRootElement().addNamespaceDeclaration(Namespace.getNamespace(prefix, uri));
  }


  private XPathExpression<Element> getXPathExpression(String xpath, Document doc) {
    return XPathFactory.instance().compile(xpath, Filters.element(), null, doc.getRootElement().getAdditionalNamespaces());
  }


  public static class ApplicationModelBuilder {

    private MuleApplicationProject project;

    public ApplicationModelBuilder(MuleApplicationProject project) {
      this.project = project;
    }

    public ApplicationModel build() throws Exception {
      Set<Path> applicationFilePaths = new HashSet<>();
      if (project.srcMainConfiguration().toFile().exists()) {
        applicationFilePaths.addAll(getFiles(project.srcMainConfiguration()));
      }
      if (project.srcTestConfiguration().toFile().exists()) {
        applicationFilePaths.addAll(getFiles(project.srcTestConfiguration()));
      }

      Map<Path, Document> applicationDocuments = new HashMap<>();
      for (Path afp : applicationFilePaths) {
        try {
          applicationDocuments.put(afp, generateDocument(afp));
        } catch (JDOMException | IOException e) {
          throw new RuntimeException("Application Model Generation Error - Fail to parse file: " + afp);
        }
      }
      return new ApplicationModel(applicationDocuments);
    }

    private Document generateDocument(Path filePath) throws JDOMException, IOException {
      SAXBuilder saxBuilder = new SAXBuilder();
      return saxBuilder.build(filePath.toFile());
    }
  }



}
