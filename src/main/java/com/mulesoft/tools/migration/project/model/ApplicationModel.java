/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import com.mulesoft.tools.migration.pom.PomModel;
import com.mulesoft.tools.migration.project.structure.mule.MuleProject;
import org.jdom2.*;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.project.structure.BasicProject.getFiles;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Represent the application to be migrated
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApplicationModel {

  private Map<Path, Document> applicationDocuments;
  private PomModel pomModel;
  private Path projectBasePath;

  protected ApplicationModel(Map<Path, Document> applicationDocuments) {
    this.applicationDocuments = applicationDocuments;
  }

  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  public List<Element> getNodes(String xpathExpression) {
    checkArgument(isNotBlank(xpathExpression), "The Xpath Expression must not be null nor empty");

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

  public void removeNameSpace(String prefix, String uri, String schemaLocation) {
    Namespace namespace = Namespace.getNamespace(prefix, uri);
    for (Document doc : applicationDocuments.values()) {
      removeNameSpace(namespace, schemaLocation, doc);
    }
  }

  protected void removeNameSpace(Namespace namespace, String schemaLocation, Document document) {
    Element rootElement = document.getRootElement();
    rootElement.removeNamespaceDeclaration(namespace);

    Attribute schemaLocationAttribute = rootElement.getAttribute("schemaLocation", rootElement.getNamespace("xsi"));

    if (schemaLocationAttribute.getValue().contains(namespace.getURI())
        && schemaLocationAttribute.getValue().contains(schemaLocation)) {

      String value = schemaLocationAttribute.getValue();
      value.replace(namespace.getURI(), EMPTY);
      value.replace(schemaLocation, EMPTY);
      schemaLocationAttribute.setValue(value);
    }
  }

  private XPathExpression<Element> getXPathExpression(String xpath, Document doc) {
    return XPathFactory.instance().compile(xpath, Filters.element(), null, doc.getRootElement().getAdditionalNamespaces());
  }

  private void setPomModel(PomModel pomModel) {
    this.pomModel = pomModel;
  }

  public Optional<PomModel> getPomModel() {
    return Optional.ofNullable(pomModel);
  }

  private void setProjectBasePath(Path projectBasePath) {
    this.projectBasePath = projectBasePath;
  }

  public Path getProjectBasePath() {
    return this.projectBasePath;
  }

  /**
   * It represent the builder to obtain a {@link ApplicationModel}
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class ApplicationModelBuilder {

    private MuleProject project;

    public ApplicationModelBuilder(MuleProject project) {
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
      ApplicationModel applicationModel = new ApplicationModel(applicationDocuments);
      PomModel pomModel = new PomModel.PomModelBuilder().withPom(project.pom()).build();
      applicationModel.setPomModel(pomModel);
      applicationModel.setProjectBasePath(project.getBaseFolder());
      return applicationModel;
    }

    private Document generateDocument(Path filePath) throws JDOMException, IOException {
      SAXBuilder saxBuilder = new SAXBuilder();
      return saxBuilder.build(filePath.toFile());
    }
  }

}
