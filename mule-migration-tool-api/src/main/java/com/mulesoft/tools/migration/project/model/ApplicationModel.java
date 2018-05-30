/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getAdditionalNamespaces;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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

  /**
   * The key of the map is relative to the source of the target project.
   */
  public Map<Path, Document> getApplicationDocuments() {
    return applicationDocuments;
  }

  /**
   * Returns all the nodes in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return all the nodes that match the xpath expression
   */
  public List<Element> getNodes(String xpathExpression) {
    return getNodes(XPathFactory.instance().compile(xpathExpression));
  }

  /**
   * Returns a single node in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return all the nodes that match the xpath expression
   */
  public Element getNode(String xpathExpression) {
    List<Element> nodes = getNodes(XPathFactory.instance().compile(xpathExpression));
    if (nodes.isEmpty() || nodes.size() > 1) {
      throw new IllegalStateException(format("Found %s nodes for xpat expression '%d'", nodes.size(), xpathExpression));
    }
    return nodes.get(0);
  }

  /**
   * Returns all the nodes in the application documents that match the xpath expression
   *
   * @param xpathExpression the xpath expression that defines which nodes should be retrieved
   * @return all the nodes that match the xpath expression
   */
  public List<Element> getNodes(XPathExpression xpathExpression) {
    checkArgument(xpathExpression != null, "The Xpath Expression must not be null nor empty");

    List<Element> nodes = new ArrayList<>();
    for (Document doc : applicationDocuments.values()) {
      nodes.addAll(getElementsFromDocument(xpathExpression, doc));
    }
    return nodes;
  }

  /**
   * Retrieves all elements in the document that have the specified namespace
   *
   * @param document  the document where the elements are going to be searched through
   * @param namespace the namespace of the elements that should be retrieved
   * @return a list of elements within the document that have that namespace
   */
  public static List<Element> getElementsWithNamespace(Document document, Namespace namespace) {
    String xPathExpression = "//*[namespace-uri()='" + namespace.getURI() + "']";
    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null,
                                                                     getAdditionalNamespaces());
    return xpath.evaluate(document);
  }

  /**
   * Adds a namespace to the application documents
   *
   * @param prefix
   * @param uri
   * @param schemaLocation
   */
  public void addNameSpace(String prefix, String uri, String schemaLocation) {
    Namespace namespace = Namespace.getNamespace(prefix, uri);
    for (Document doc : applicationDocuments.values()) {
      addNameSpace(namespace, schemaLocation, doc);
    }
  }

  /**
   * Adds a namespace to an application document
   *
   * @param namespace
   * @param schemaLocation
   * @param document
   */
  public void addNameSpace(Namespace namespace, String schemaLocation, Document document) {
    Element rootElement = document.getRootElement();
    rootElement.addNamespaceDeclaration(namespace);

    Attribute schemaLocationAttribute = rootElement.getAttribute("schemaLocation", rootElement.getNamespace("xsi"));
    if (!schemaLocationAttribute.getValue().contains(namespace.getURI() + " ")
        && !schemaLocationAttribute.getValue().contains(schemaLocation)) {

      StringBuilder value = new StringBuilder(schemaLocationAttribute.getValue());
      value.append(lineSeparator() + " " + namespace.getURI());
      value.append(" " + schemaLocation);
      schemaLocationAttribute.setValue(value.toString());
    }
  }

  /**
   * Removes a namespace from the application documents
   *
   * @param prefix
   * @param uri
   * @param schemaLocation
   */
  public void removeNameSpace(String prefix, String uri, String schemaLocation) {
    Namespace namespace = Namespace.getNamespace(prefix, uri);
    for (Document doc : applicationDocuments.values()) {
      removeNameSpace(namespace, schemaLocation, doc);
    }
  }

  /**
   * Removes a namespace from an application document
   *
   * @param namespace
   * @param schemaLocation
   * @param document
   */
  public void removeNameSpace(Namespace namespace, String schemaLocation, Document document) {
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

  /**
   * Retrieves all elements that satisfies the given xpath expression in the document
   *
   * @param xpath
   * @param document
   * @return all the elements queried by the xpath expression in the given document
   * @throws IllegalArgumentException if the XPath query cannot be compiled
   */
  private List<Element> getElementsFromDocument(XPathExpression xpath, Document document) {
    XPathExpression<Element> compiledXPath =
        XPathFactory.instance().compile(xpath.getExpression(), Filters.element(), null, getAdditionalNamespaces());
    try {
      return compiledXPath.evaluate(document);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().matches("Namespace with prefix '\\w+' has not been declared.")) {
        return emptyList();
      } else {
        throw e;
      }
    }
  }

  /**
   * Sets the {@link PomModel} that represents the pom.xml file present in this application
   *
   * @param pomModel
   */
  private void setPomModel(PomModel pomModel) {
    this.pomModel = pomModel;
  }

  /**
   * Retrieves the {@link PomModel} of this instance. It is optional since the pom is not mandatory in a Mule 3.x application
   *
   * @return tan optional {@link PomModel}
   */
  public Optional<PomModel> getPomModel() {
    return Optional.ofNullable(pomModel);
  }

  /**
   * The path to the root of the project represented by the application model instance
   *
   * @param projectBasePath
   */
  private void setProjectBasePath(Path projectBasePath) {
    this.projectBasePath = projectBasePath;
  }

  /**
   * Retrieves the root path of the project represented by the application model instance
   */
  public Path getProjectBasePath() {
    return this.projectBasePath;
  }

  /**
   * Sets the  {@link MuleArtifactJsonModel} that represents the mule-artifact.json file
   *
   * @param muleArtifactJsonModel
   */
  private void setMuleArtifactJsonModel(MuleArtifactJsonModel muleArtifactJsonModel) {
    this.muleArtifactJsonModel = muleArtifactJsonModel;
  }

  /**
   * Retrieves the {@link MuleArtifactJsonModel}. It is optional since this resource is not part of a Mule 3.x application
   *
   * @return an optional {@link MuleArtifactJsonModel}
   */
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

    /**
     * Collection of paths to project configuration files
     *
     * @param configurationFiles
     * @return the builder
     */
    public ApplicationModelBuilder withConfigurationFiles(Collection<Path> configurationFiles) {
      this.configurationFiles = configurationFiles;
      return this;
    }

    /**
     * Collection of paths to project test configuration files
     *
     * @param testConfigurationFiles
     * @return the builder
     */
    public ApplicationModelBuilder withTestConfigurationFiles(Collection<Path> testConfigurationFiles) {
      this.testConfigurationFiles = testConfigurationFiles;
      return this;
    }

    /**
     * Path to project mule-artifact.json file
     *
     * @param muleArtifactJson
     * @return the builder
     */
    public ApplicationModelBuilder withMuleArtifactJson(Path muleArtifactJson) {
      this.muleArtifactJson = muleArtifactJson;
      return this;
    }

    /**
     * Path to project pom.xml file
     *
     * @param pom
     * @return the builder
     */
    public ApplicationModelBuilder withPom(Path pom) {
      this.pom = pom;
      return this;
    }

    /**
     * Path to project base folder
     *
     * @param projectBasePath
     * @return the builder
     */
    public ApplicationModelBuilder withProjectBasePath(Path projectBasePath) {
      this.projectBasePath = projectBasePath;
      return this;
    }

    /**
     * String representing the project version
     *
     * @param muleVersion
     * @return the builder
     */
    public ApplicationModelBuilder withMuleVersion(String muleVersion) {
      this.muleVersion = muleVersion;
      return this;
    }

    /**
     * Build the {@link ApplicationModel}
     *
     * @return an {@link ApplicationModel} instance
     * @throws Exception if project base path is null; if pom.xml or mule-artifact.json file do not exist in the provided path or are invalid
     */
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
          applicationDocuments.put(projectBasePath.relativize(afp), generateDocument(afp));
        } catch (JDOMException | IOException e) {
          throw new RuntimeException("Application Model Generation Error - Fail to parse file: " + afp, e);
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
