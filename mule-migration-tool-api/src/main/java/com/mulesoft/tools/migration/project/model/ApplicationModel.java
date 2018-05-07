/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getAdditionalNamespaces;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
  private MuleArtifactJsonModel muleArtifactJsonModel;

  protected ApplicationModel(Map<Path, Document> applicationDocuments) {
    this.applicationDocuments = applicationDocuments;
  }

  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  public List<Element> getNodes(XPathExpression xpathExpression) {
    checkArgument(xpathExpression != null, "The Xpath Expression must not be null nor empty");

    List<Element> nodes = new ArrayList<>();
    for (Document doc : applicationDocuments.values()) {
      nodes.addAll(getElementsFromDocument(xpathExpression, doc));
    }
    return nodes;
  }

  public static List<Element> getElementsWithNamespace(Document document, Namespace n) {
    String xPathExpression = "//*[namespace-uri()='" + n.getURI() + "']";
    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null,
                                                                     getAdditionalNamespaces());
    return xpath.evaluate(document);
  }

  public void addNameSpace(String prefix, String uri, String schemaLocation) {
    Namespace namespace = Namespace.getNamespace(prefix, uri);
    for (Document doc : applicationDocuments.values()) {
      addNameSpace(namespace, schemaLocation, doc);
    }
  }

  public void addNameSpace(Namespace namespace, String schemaLocation, Document document) {
    Element rootElement = document.getRootElement();
    rootElement.addNamespaceDeclaration(namespace);

    Attribute schemaLocationAttribute = rootElement.getAttribute("schemaLocation", rootElement.getNamespace("xsi"));
    if (!schemaLocationAttribute.getValue().contains(namespace.getURI())
        && !schemaLocationAttribute.getValue().contains(schemaLocation)) {

      StringBuilder value = new StringBuilder(schemaLocationAttribute.getValue());
      value.append(" " + namespace.getURI());
      value.append(" " + schemaLocation);
      schemaLocationAttribute.setValue(value.toString());
    }
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
      value = value.replace(schemaLocation, EMPTY);
      value = value.replace(namespace.getURI(), EMPTY);
      schemaLocationAttribute.setValue(value);
    }
  }

  private List<Element> getElementsFromDocument(XPathExpression xpath, Document doc) {
    return XPathFactory.instance().compile(xpath.getExpression(), Filters.element(), null, getAdditionalNamespaces())
        .evaluate(doc);
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

  private void setMuleArtifactJsonModel(MuleArtifactJsonModel muleArtifactJsonModel) {
    this.muleArtifactJsonModel = muleArtifactJsonModel;
  }

  public Optional<MuleArtifactJsonModel> getMuleArtifactJsonModel() {
    return Optional.ofNullable(muleArtifactJsonModel);
  }


  /**
   * It represent the builder to obtain a {@link ApplicationModel}
   *
   * @author Mulesoft Inc.
   * @since 1.0.0
   */
  public static class ApplicationModelBuilder {

    private Collection<Path> configurationFiles;
    private Collection<Path> testConfigurationFiles;
    private Path muleArtifactJson;
    private Path pom;
    private Path projectBasePath;
    private String muleVersion;

    public ApplicationModelBuilder withConfigurationFiles(Collection<Path> configurationFiles) {
      this.configurationFiles = configurationFiles;
      return this;
    }

    public ApplicationModelBuilder withTestConfigurationFiles(Collection<Path> testConfigurationFiles) {
      this.testConfigurationFiles = testConfigurationFiles;
      return this;
    }

    public ApplicationModelBuilder withMuleArtifactJson(Path muleArtifactJson) {
      this.muleArtifactJson = muleArtifactJson;
      return this;
    }

    public ApplicationModelBuilder withPom(Path pom) {
      this.pom = pom;
      return this;
    }

    public ApplicationModelBuilder withProjectBasePath(Path projectBasePath) {
      this.projectBasePath = projectBasePath;
      return this;
    }

    public ApplicationModelBuilder withMuleVersion(String muleVersion) {
      this.muleVersion = muleVersion;
      return this;
    }

    public ApplicationModel build() throws Exception {
      checkArgument(projectBasePath != null, "Project base path cannot be null");

      Set<Path> applicationFilePaths = new HashSet<>();
      if (configurationFiles != null) {
        applicationFilePaths.addAll(configurationFiles);
      }
      if (testConfigurationFiles != null) {
        applicationFilePaths.addAll(testConfigurationFiles);
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
      if (muleArtifactJson != null) {
        MuleArtifactJsonModel muleArtifactJsonModel = new MuleArtifactJsonModel.MuleApplicationJsonModelBuilder()
            .withMuleArtifactJson(muleArtifactJson)
            .withConfigs(configurationFiles)
            .withMuleVersion(muleVersion)
            .build();
        applicationModel.setMuleArtifactJsonModel(muleArtifactJsonModel);
      }
      PomModel pomModel;
      if (pom != null && pom.toFile().exists()) {
        pomModel = new PomModel.PomModelBuilder().withPom(pom).build();
      } else {
        pomModel = new PomModel.PomModelBuilder().withArtifactId(projectBasePath.getFileName().toString()).build();
      }
      applicationModel.setPomModel(pomModel);
      applicationModel.setProjectBasePath(projectBasePath);

      return applicationModel;
    }

    private Document generateDocument(Path filePath) throws JDOMException, IOException {
      SAXBuilder saxBuilder = new SAXBuilder();
      saxBuilder.setJDOMFactory(new LocatedJDOMFactory());
      return saxBuilder.build(filePath.toFile());
    }
  }

}
